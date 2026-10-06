package com.oposiciones.voz;

import com.oposiciones.temario.Fragmento;
import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemaRepository;
import com.oposiciones.temario.TemarioException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Playlist por fragmentos, generación perezosa y progreso de escucha. */
@Service
public class NarracionService {

  private final TemaRepository temas;
  private final FragmentoRepository fragmentos;
  private final AudioFragmentoRepository audios;
  private final ProgresoRepository progresos;
  private final TtsService tts;
  private final AudioStore store;

  public NarracionService(TemaRepository temas, FragmentoRepository fragmentos,
      AudioFragmentoRepository audios, ProgresoRepository progresos, TtsService tts,
      AudioStore store) {
    this.temas = temas;
    this.fragmentos = fragmentos;
    this.audios = audios;
    this.progresos = progresos;
    this.tts = tts;
    this.store = store;
  }

  public record ItemPlaylist(Long fragmentoId, int orden, int pagina, Double duracionSeg,
      String audioUrl) {}

  @Transactional(readOnly = true)
  public List<ItemPlaylist> playlist(Long temaId) {
    Tema tema = exigirListoConContenido(temaId);
    return fragmentos.findByTemaIdOrderByOrdenAsc(tema.getId()).stream()
        .map(f -> new ItemPlaylist(f.getId(), f.getOrden(), f.getPagina(),
            audios.findById(f.getId()).map(AudioFragmento::getDuracionSeg).orElse(null),
            "/api/v1/fragmentos/" + f.getId() + "/audio"))
        .toList();
  }

  public record EntregaAudio(byte[] datos, String formato) {}

  @Transactional
  public EntregaAudio audioDe(Long fragmentoId) {
    Fragmento f = fragmentos.findById(fragmentoId)
        .orElseThrow(() -> new TemarioException("Fragmento no existe: " + fragmentoId, 404));
    AudioFragmento row = asegurarAudio(f);
    return new EntregaAudio(store.leer(f.getTemaId(), f.getId(), row.getFormato()), row.getFormato());
  }

  @Transactional
  public AudioFragmento asegurarAudio(Fragmento f) {
    var existente = audios.findById(f.getId());
    if (existente.isPresent() && store.existe(f.getTemaId(), f.getId(), existente.get().getFormato())) {
      return existente.get();
    }
    TtsService.Audio audio = tts.sintetizar(f.getTexto());
    store.guardar(f.getTemaId(), f.getId(), audio.extension(), audio.datos());
    existente.ifPresent(audios::delete);
    return audios.save(new AudioFragmento(f.getId(), audio.duracionSeg(), audio.extension()));
  }

  @Transactional
  public ProgresoEscucha guardarProgreso(Long temaId, Long fragmentoId, double offsetSeg) {
    exigirExiste(temaId);
    Fragmento f = fragmentos.findById(fragmentoId)
        .orElseThrow(() -> new TemarioException("Fragmento no existe: " + fragmentoId, 404));
    if (!f.getTemaId().equals(temaId)) {
      throw new TemarioException("El fragmento no pertenece al tema", 422);
    }
    asegurarAudio(f);
    double duracion = audios.findById(f.getId()).orElseThrow().getDuracionSeg();
    if (offsetSeg < 0 || offsetSeg > duracion) {
      throw new TemarioException("Offset fuera del audio (0-" + duracion + "s)", 422);
    }
    ProgresoEscucha p = progresos.findById(temaId)
        .orElse(new ProgresoEscucha(temaId, fragmentoId, offsetSeg));
    p.setFragmentoId(fragmentoId);
    p.setOffsetSeg(offsetSeg);
    p.setActualizadoEn(java.time.LocalDateTime.now());
    return progresos.save(p);
  }

  @Transactional(readOnly = true)
  public Optional<ProgresoEscucha> progresoDe(Long temaId) {
    exigirExiste(temaId);
    return progresos.findById(temaId);
  }

  @Transactional
  public void terminarProgreso(Long temaId) {
    exigirExiste(temaId);
    progresos.deleteById(temaId);
  }

  private void exigirExiste(Long temaId) {
    if (!temas.existsById(temaId)) throw new TemarioException("Tema no existe: " + temaId, 404);
  }

  private Tema exigirListoConContenido(Long temaId) {
    Tema tema = temas.findById(temaId)
        .orElseThrow(() -> new TemarioException("Tema no existe: " + temaId, 404));
    if (tema.getEstado() != Tema.Estado.LISTO) {
      throw new TemarioException("Tema no listo para narrar (estado " + tema.getEstado() + ")", 422);
    }
    if (fragmentos.countByTemaId(temaId) == 0) {
      throw new TemarioException("Tema sin contenido narrable", 422);
    }
    return tema;
  }
}
