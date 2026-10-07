package com.examprep.exam;

import com.examprep.syllabus.SyllabusException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ExamController {

  private final TestGenerator generador;
  private final Grader corrector;
  private final GeneratedTestRepository tests;

  public ExamController(TestGenerator generador, Grader corrector,
      GeneratedTestRepository tests) {
    this.generador = generador;
    this.corrector = corrector;
    this.tests = tests;
  }

  public record QuestionDto(Long id, int sequence, String statement, List<String> options,
      Long citedTopicId, Long citedFragmentId, int citedPage) {}
  public record TestDto(Long id, Long topicId, String alcance, String difficulty, int questionCount,
      String status, String notice, List<QuestionDto> questions) {}
  public record HistoryDto(Long id, Long topicId, String alcance, String difficulty, int questionCount, String status,
      Double score, String createdAt) {}

  @PostMapping("/temas/{id}/tests")
  public ResponseEntity<TestDto> generate(@PathVariable Long id,
      @RequestParam(defaultValue = "10") int n,
      @RequestParam(defaultValue = "MEDIUM") String difficulty) {
    return ResponseEntity.status(201).body(toDto(generador.generate(id, n, parseDifficulty(difficulty))));
  }

  @PostMapping("/tests")
  public ResponseEntity<TestDto> generateMixed(
      @RequestParam(defaultValue = "10") int n,
      @RequestParam(defaultValue = "MEDIUM") String difficulty,
      @RequestParam(value = "topicIds", required = false) List<Long> topicIds) {
    return ResponseEntity.status(201).body(toDto(generador.generateMixed(topicIds, n, parseDifficulty(difficulty))));
  }

  private Difficulty parseDifficulty(String difficulty) {
    try {
      return Difficulty.valueOf(difficulty.toUpperCase());
    } catch (Exception e) {
      throw new SyllabusException("Difficulty EASY, MEDIUM o HARD", 422);
    }
  }

  private TestDto toDto(TestGenerator.CreatedTest created) {
    var t = created.test();
    return new TestDto(t.getId(), t.getTopicId(), t.getAlcance(),
        t.getDifficulty().name(), t.getQuestionCount(), t.getStatus().name(), created.notice(),
        created.questions().stream().map(this::dto).toList());
  }

  @GetMapping("/temas/{id}/tests")
  public List<HistoryDto> history(@PathVariable Long id) {
    return tests.findByTopicIdOrderByCreatedAtDesc(id).stream()
        .map(this::historyDto)
        .toList();
  }

  @GetMapping("/tests")
  public List<HistoryDto> allHistory() {
    return tests.findAll().stream()
        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
        .map(this::historyDto)
        .toList();
  }

  private HistoryDto historyDto(GeneratedTest t) {
    return new HistoryDto(t.getId(), t.getTopicId(), t.getAlcance(), t.getDifficulty().name(),
        t.getQuestionCount(), t.getStatus().name(), scoreIfGraded(t), t.getCreatedAt().toString());
  }

  @PostMapping("/tests/{testId}/responder")
  public Grader.Feedback answer(@PathVariable Long testId,
      @RequestBody Map<String, Object> body) {
    Object pid = body.get("questionId");
    Object opc = body.get("option");
    if (pid == null || opc == null) throw new SyllabusException("Faltan questionId y option", 422);
    try {
      return corrector.answer(testId, Long.valueOf(pid.toString()),
          Integer.parseInt(opc.toString()));
    } catch (SyllabusException e) {
      throw e;
    } catch (Exception e) {
      throw new SyllabusException("questionId y option deben ser números", 422);
    }
  }

  @PostMapping("/tests/{testId}/finalizar")
  public Grader.Grade finishTest(@PathVariable Long testId) {
    return corrector.finish(testId);
  }

  private QuestionDto dto(Question p) {
    List<String> ops;
    try {
      ops = new com.fasterxml.jackson.databind.ObjectMapper().readValue(p.getOptions(),
          new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
    } catch (Exception e) {
      ops = List.of();
    }
    return new QuestionDto(p.getId(), p.getSequence(), p.getStatement(), ops,
        p.getCitedTopicId(), p.getCitedFragmentId(), p.getCitedPage());
  }

  private Double scoreIfGraded(GeneratedTest t) {
    if (t.getStatus() != GeneratedTest.Status.GRADED) return null;
    return corrector.summary(t.getId()).score();
  }

  @ExceptionHandler(SyllabusException.class)
  public ResponseEntity<Map<String, String>> handle(SyllabusException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
