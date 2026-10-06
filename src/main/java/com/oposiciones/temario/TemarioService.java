package com.oposiciones.temario;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orquesta la ingesta: valida, extrae, trocea, embebe y persiste por lotes. */
@Service
public class TemarioService {

  static final int MIN_TEXT_CHARS = 50;

  private final TemaRepository temas;
  private final FragmentoRepository fragmentos;
  private final EmbeddingRepository embeddings;
  private final PdfTextExtractor extractor;
  private final TextChunker chunker;
  private final EmbeddingService embeddingService;
  private final ApplicationEventPublisher eventos;

  public TemarioService(TemaRepository temas, FragmentoRepository fragmentos,
      EmbeddingRepository embeddings, PdfTextExtractor extractor, TextChunker chunker,
      EmbeddingService embeddingService, ApplicationEventPublisher eventos) {
    this.temas = temas;
    this.fragmentos = fragmentos;
    this.embeddings = embeddings;
    this.extractor = extractor;
    this.chunker = chunker;
    this.embeddingService = embeddingService;
    this.eventos = eventos;
  }

  public record TemaVista(Tema tema, long numFragmentos) {}

  @Transactional
  public Tema ingestar(String filename, byte[] bytes) {
    String sha = Sha256.of(bytes);
    if (temas.findByContentSha256(sha).isPresent()) {
      throw new TemarioException("Ya cargado (mismo contenido): " + filename, 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new TemarioException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declarados = chunker.temasDeclarados(doc.fullText());
    if (declarados.size() > 1) {
      throw new TemarioException(
          "Divide el fichero, 1 PDF = 1 tema en v1 (detectados temas " + declarados + "): " + filename, 422);
    }
    String titulo = filename.replaceFirst("\\.[^.]+$", "");
    Tema tema = new Tema(titulo, filename, sha, doc.pages().size());
    temas.save(tema);
    indexar(tema, doc);
    tema.setEstado(Tema.Estado.LISTO);
    eventos.publishEvent(new TemaListoEvent(tema.getId()));
    return tema;
  }

  @Transactional
  public Tema reemplazar(Long id, String filename, byte[] bytes) {
    Tema tema = temas.findById(id)
        .orElseThrow(() -> new TemarioException("Tema no existe: " + id, 404));
    String sha = Sha256.of(bytes);
    if (sha.equals(tema.getContentSha256())) {
      throw new TemarioException("El PDF es idéntico al actual, nada que reemplazar", 422);
    }
    if (temas.findByContentSha256(sha).filter(t -> !t.getId().equals(id)).isPresent()) {
      throw new TemarioException("Ese contenido ya está cargado en otro tema", 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new TemarioException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declarados = chunker.temasDeclarados(doc.fullText());
    if (declarados.size() > 1) {
      throw new TemarioException(
          "Divide el fichero, 1 PDF = 1 tema en v1 (detectados temas " + declarados + ")", 422);
    }
    // El título personalizado se conserva (aclaración A): solo cambian contenido y origen.
    tema.setContentSha256(sha);
    tema.setNumPaginas(doc.pages().size());
    tema.setEstado(Tema.Estado.PROCESANDO);
    tema.setMensajeError(null);
    borrarContenido(tema.getId());
    indexar(tema, doc);
    tema.setEstado(Tema.Estado.LISTO);
    eventos.publishEvent(new TemaListoEvent(tema.getId()));
    return tema;
  }

  @Transactional
  public Tema renombrar(Long id, String titulo) {
    Tema tema = temas.findById(id)
        .orElseThrow(() -> new TemarioException("Tema no existe: " + id, 404));
    if (titulo == null || titulo.isBlank()) {
      throw new TemarioException("El título no puede estar vacío", 422);
    }
    tema.setTitulo(titulo.strip());
    return tema;
  }

  @Transactional
  public void borrar(Long id) {
    Tema tema = temas.findById(id)
        .orElseThrow(() -> new TemarioException("Tema no existe: " + id, 404));
    borrarContenido(id);
    temas.delete(tema);
  }

  @Transactional(readOnly = true)
  public List<TemaVista> listar() {
    return temas.findAll().stream()
        .map(t -> new TemaVista(t, fragmentos.countByTemaId(t.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Fragmento> fragmentosDe(Long id) {
    exigirExiste(id);
    return fragmentos.findByTemaIdOrderByOrdenAsc(id);
  }

  private void exigirExiste(Long id) {
    if (!temas.existsById(id)) throw new TemarioException("Tema no existe: " + id, 404);
  }

  private void indexar(Tema tema, PdfTextExtractor.PdfDocument doc) {
    var chunks = chunker.chunk(doc.pages());
    for (var c : chunks) {
      Fragmento f = fragmentos.save(new Fragmento(tema.getId(), c.orden(), c.pagina(), c.texto()));
      float[] v = embeddingService.embed(c.texto());
      embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(v)));
    }
  }

  private void borrarContenido(Long temaId) {
    var frags = fragmentos.findByTemaIdOrderByOrdenAsc(temaId);
    embeddings.deleteByChunkIdIn(frags.stream().map(Fragmento::getId).toList());
    fragmentos.deleteByTemaId(temaId);
    eventos.publishEvent(new TemaContenidoBorradoEvent(temaId));
  }
}
