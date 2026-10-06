package com.oposiciones.examen;

import com.oposiciones.temario.TemarioException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ExamenController {

  private final GeneradorTests generador;
  private final CorrectorService corrector;
  private final TestGeneradoRepository tests;

  public ExamenController(GeneradorTests generador, CorrectorService corrector,
      TestGeneradoRepository tests) {
    this.generador = generador;
    this.corrector = corrector;
    this.tests = tests;
  }

  public record PreguntaDto(Long id, int orden, String enunciado, List<String> opciones,
      Long citaTemaId, Long citaFragmentoId, int citaPagina) {}
  public record TestDto(Long id, Long temaId, String dificultad, int numPreguntas,
      String estado, String aviso, List<PreguntaDto> preguntas) {}
  public record HistorialDto(Long id, String dificultad, int numPreguntas, String estado,
      Double nota, String creadoEn) {}

  @PostMapping("/temas/{id}/tests")
  public ResponseEntity<TestDto> generar(@PathVariable Long id,
      @RequestParam(defaultValue = "10") int n,
      @RequestParam(defaultValue = "MEDIO") String dificultad) {
    Dificultad d;
    try {
      d = Dificultad.valueOf(dificultad.toUpperCase());
    } catch (Exception e) {
      throw new TemarioException("Dificultad FACIL, MEDIO o DIFICIL", 422);
    }
    var creado = generador.generar(id, n, d);
    var t = creado.test();
    return ResponseEntity.status(201).body(new TestDto(t.getId(), t.getTemaId(),
        t.getDificultad().name(), t.getNumPreguntas(), t.getEstado().name(), creado.aviso(),
        creado.preguntas().stream().map(this::dto).toList()));
  }

  @GetMapping("/temas/{id}/tests")
  public List<HistorialDto> historial(@PathVariable Long id) {
    return tests.findByTemaIdOrderByCreadoEnDesc(id).stream()
        .map(t -> new HistorialDto(t.getId(), t.getDificultad().name(), t.getNumPreguntas(),
            t.getEstado().name(), notaSiCorregido(t), t.getCreadoEn().toString()))
        .toList();
  }

  @PostMapping("/tests/{testId}/responder")
  public CorrectorService.Feedback responder(@PathVariable Long testId,
      @RequestBody Map<String, Object> body) {
    Object pid = body.get("preguntaId");
    Object opc = body.get("opcion");
    if (pid == null || opc == null) throw new TemarioException("Faltan preguntaId y opcion", 422);
    try {
      return corrector.responder(testId, Long.valueOf(pid.toString()),
          Integer.parseInt(opc.toString()));
    } catch (TemarioException e) {
      throw e;
    } catch (Exception e) {
      throw new TemarioException("preguntaId y opcion deben ser números", 422);
    }
  }

  @PostMapping("/tests/{testId}/finalizar")
  public CorrectorService.Nota finalizar(@PathVariable Long testId) {
    return corrector.finalizar(testId);
  }

  private PreguntaDto dto(Pregunta p) {
    List<String> ops;
    try {
      ops = new com.fasterxml.jackson.databind.ObjectMapper().readValue(p.getOpciones(),
          new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
    } catch (Exception e) {
      ops = List.of();
    }
    return new PreguntaDto(p.getId(), p.getOrden(), p.getEnunciado(), ops,
        p.getCitaTemaId(), p.getCitaFragmentoId(), p.getCitaPagina());
  }

  private Double notaSiCorregido(TestGenerado t) {
    if (t.getEstado() != TestGenerado.Estado.CORREGIDO) return null;
    return corrector.resumen(t.getId()).nota();
  }

  @ExceptionHandler(TemarioException.class)
  public ResponseEntity<Map<String, String>> handle(TemarioException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
