package com.oposiciones.voz;

import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.TemaContenidoBorradoEvent;
import com.oposiciones.temario.TemaListoEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Genera los WAV en segundo plano tras la ingesta; limpia al borrar/reemplazar. */
@Component
public class TemaListoListener {

  private final FragmentoRepository fragmentos;
  private final NarracionService narracion;
  private final ProgresoRepository progresos;
  private final AudioStore store;

  public TemaListoListener(FragmentoRepository fragmentos, NarracionService narracion,
      ProgresoRepository progresos, AudioStore store) {
    this.fragmentos = fragmentos;
    this.narracion = narracion;
    this.progresos = progresos;
    this.store = store;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void generarAudios(TemaListoEvent event) {
    for (var f : fragmentos.findByTemaIdOrderByOrdenAsc(event.temaId())) {
      narracion.asegurarAudio(f);
    }
  }

  @EventListener
  public void limpiarRestos(TemaContenidoBorradoEvent event) {
    progresos.deleteById(event.temaId());
    store.borrarTema(event.temaId());
  }
}
