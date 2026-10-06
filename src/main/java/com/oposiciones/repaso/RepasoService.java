package com.oposiciones.repaso;

import com.oposiciones.temario.EmbeddingRepository;
import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Fragmento;
import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.TemaRepository;
import com.oposiciones.temario.TemarioException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ranking híbrido en memoria: 0.5·coseno + 0.5·bonus literal.
 * Corte 0.35: por debajo, sin resultados (cero alucinaciones).
 */
@Service
public class RepasoService {

  static final double UMBRAL = 0.35;
  static final int TOPK_DEFECTO = 5;

  private final FragmentoRepository fragmentos;
  private final TemaRepository temas;
  private final EmbeddingRepository embeddings;
  private final EmbeddingService embeddingService;

  public RepasoService(FragmentoRepository fragmentos, TemaRepository temas,
      EmbeddingRepository embeddings, EmbeddingService embeddingService) {
    this.fragmentos = fragmentos;
    this.temas = temas;
    this.embeddings = embeddings;
    this.embeddingService = embeddingService;
  }

  public record Candidato(Long fragmentoId, Long temaId, int pagina, String texto,
      double score, String audioUrl) {}

  @Transactional(readOnly = true)
  public List<Candidato> repasar(String q, Long temaId, int topK) {
    String consulta = q == null ? "" : q.strip();
    if (consulta.length() < 2 || consulta.length() > 500) {
      throw new TemarioException("Indica una palabra o frase de 2 a 500 caracteres", 422);
    }
    if (temaId != null && !temas.existsById(temaId)) {
      throw new TemarioException("Tema no existe: " + temaId, 404);
    }
    List<Fragmento> base = temaId == null
        ? fragmentos.findAll()
        : fragmentos.findByTemaIdOrderByOrdenAsc(temaId);
    float[] vq = embeddingService.embed(consulta);
    String nq = Normalizador.normalizar(consulta);
    List<String> palabras = List.of(nq.split("\\s+"));
    List<Candidato> out = new ArrayList<>();
    var embMap = embeddings.findAll().stream()
        .collect(java.util.stream.Collectors.toMap(
            com.oposiciones.temario.ChunkEmbedding::getChunkId, e -> e,
            (a, b) -> a));
    for (Fragmento f : base) {
      var emb = embMap.get(f.getId());
      if (emb == null) continue;
      float[] v = embeddingService.decode(emb.getEmbedding());
      if (v.length != vq.length) continue;
      double coseno = EmbeddingService.dot(vq, v);
      String nt = Normalizador.normalizar(f.getTexto());
      double bonus = nt.contains(nq) ? 1.0
          : palabras.stream().allMatch(nt::contains) ? 0.3 : 0.0;
      double score = 0.5 * coseno + 0.5 * bonus;
      if (score >= UMBRAL) {
        out.add(new Candidato(f.getId(), f.getTemaId(), f.getPagina(), f.getTexto(), score,
            "/api/v1/fragmentos/" + f.getId() + "/audio"));
      }
    }
    out.sort(Comparator.comparingDouble(Candidato::score).reversed());
    return out.subList(0, Math.min(Math.max(topK, 1), out.size()));
  }
}
