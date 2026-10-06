package com.oposiciones.examen;

import com.oposiciones.temario.TemarioException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Feedback inmediato por pregunta + nota al finalizar. */
@Service
public class CorrectorService {

  private final TestGeneradoRepository tests;
  private final PreguntaRepository preguntas;
  private final RespuestaRepository respuestas;

  public CorrectorService(TestGeneradoRepository tests, PreguntaRepository preguntas,
      RespuestaRepository respuestas) {
    this.tests = tests;
    this.preguntas = preguntas;
    this.respuestas = respuestas;
  }

  public record Feedback(boolean acierto, int correcta, String explicacion,
      Long citaTemaId, Long citaFragmentoId, int citaPagina) {}
  public record Detalle(Long preguntaId, String enunciado, Integer elegida, int correcta,
      boolean acierto, String explicacion) {}
  public record Nota(double nota, int aciertos, int fallos, List<Detalle> detalle) {}

  @Transactional
  public Feedback responder(Long testId, Long preguntaId, int opcion) {
    TestGenerado test = exigirPendiente(testId);
    Pregunta p = preguntas.findById(preguntaId)
        .orElseThrow(() -> new TemarioException("Pregunta no existe: " + preguntaId, 404));
    if (!p.getTestId().equals(test.getId())) {
      throw new TemarioException("La pregunta no pertenece al test", 422);
    }
    if (opcion < 0 || opcion > 3) throw new TemarioException("Opción 0-3", 422);
    boolean acierto = opcion == p.getCorrecta();
    respuestas.findById(preguntaId).ifPresentOrElse(
        r -> { r.setOpcion(opcion); r.setAcierto(acierto ? 1 : 0); },
        () -> respuestas.save(new Respuesta(preguntaId, opcion, acierto ? 1 : 0)));
    return new Feedback(acierto, p.getCorrecta(), p.getExplicacion(),
        p.getCitaTemaId(), p.getCitaFragmentoId(), p.getCitaPagina());
  }

  @Transactional
  public Nota finalizar(Long testId) {
    TestGenerado test = exigirPendiente(testId);
    test.setEstado(TestGenerado.Estado.CORREGIDO);
    return calcular(testId);
  }

  @Transactional(readOnly = true)
  public Nota resumen(Long testId) {
    if (!tests.existsById(testId)) {
      throw new TemarioException("Test no existe: " + testId, 404);
    }
    return calcular(testId);
  }

  private Nota calcular(Long testId) {
    List<Pregunta> ps = preguntas.findByTestIdOrderByOrdenAsc(testId);
    int aciertos = 0;
    var detalle = new java.util.ArrayList<Detalle>();
    for (Pregunta p : ps) {
      var r = respuestas.findById(p.getId());
      Integer elegida = r.map(Respuesta::getOpcion).orElse(null);
      boolean ok = r.map(x -> x.getAcierto() == 1).orElse(false);
      if (ok) aciertos++;
      detalle.add(new Detalle(p.getId(), p.getEnunciado(), elegida, p.getCorrecta(), ok,
          p.getExplicacion()));
    }
    double nota = ps.isEmpty() ? 0 : 10.0 * aciertos / ps.size();
    return new Nota(Math.round(nota * 100.0) / 100.0, aciertos, ps.size() - aciertos, detalle);
  }

  private TestGenerado exigirPendiente(Long testId) {
    TestGenerado test = tests.findById(testId)
        .orElseThrow(() -> new TemarioException("Test no existe: " + testId, 404));
    if (test.getEstado() != TestGenerado.Estado.PENDIENTE) {
      throw new TemarioException("Test ya finalizado", 422);
    }
    return test;
  }
}
