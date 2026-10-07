package com.examprep.exam;

import com.examprep.syllabus.SyllabusException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Immediate feedback per question + score on completion. */
@Service
public class Grader {

  private final GeneratedTestRepository tests;
  private final QuestionRepository questions;
  private final AnswerRepository answers;

  public Grader(GeneratedTestRepository tests, QuestionRepository questions,
      AnswerRepository answers) {
    this.tests = tests;
    this.questions = questions;
    this.answers = answers;
  }

  public record Feedback(boolean correct, int correctIndex, String explanation,
      Long citedTopicId, Long citedFragmentId, int citedPage) {}
  public record Detail(Long questionId, String statement, Integer selected, int correctIndex,
      boolean correct, String explanation) {}
  public record Grade(double score, int correctCount, int wrongCount, List<Detail> details) {}

  @Transactional
  public Feedback answer(Long testId, Long questionId, int option) {
    GeneratedTest test = requirePending(testId);
    Question p = questions.findById(questionId)
        .orElseThrow(() -> new SyllabusException("Pregunta no existe: " + questionId, 404));
    if (!p.getTestId().equals(test.getId())) {
      throw new SyllabusException("La pregunta no pertenece al test", 422);
    }
    if (option < 0 || option > 3) throw new SyllabusException("Opción 0-3", 422);
    boolean correct = option == p.getCorrectIndex();
    answers.findById(questionId).ifPresentOrElse(
        r -> { r.setOption(option); r.setCorrect(correct ? 1 : 0); },
        () -> answers.save(new Answer(questionId, option, correct ? 1 : 0)));
    return new Feedback(correct, p.getCorrectIndex(), p.getExplanation(),
        p.getCitedTopicId(), p.getCitedFragmentId(), p.getCitedPage());
  }

  @Transactional
  public Grade finish(Long testId) {
    GeneratedTest test = requirePending(testId);
    test.setStatus(GeneratedTest.Status.GRADED);
    return calcular(testId);
  }

  @Transactional(readOnly = true)
  public Grade summary(Long testId) {
    if (!tests.existsById(testId)) {
      throw new SyllabusException("Test no existe: " + testId, 404);
    }
    return calcular(testId);
  }

  private Grade calcular(Long testId) {
    List<Question> ps = questions.findByTestIdOrderBySequenceAsc(testId);
    int correctCount = 0;
    var details = new java.util.ArrayList<Detail>();
    for (Question p : ps) {
      var r = answers.findById(p.getId());
      Integer selected = r.map(Answer::getOption).orElse(null);
      boolean ok = r.map(x -> x.getCorrect() == 1).orElse(false);
      if (ok) correctCount++;
      details.add(new Detail(p.getId(), p.getStatement(), selected, p.getCorrectIndex(), ok,
          p.getExplanation()));
    }
    double score = ps.isEmpty() ? 0 : 10.0 * correctCount / ps.size();
    return new Grade(Math.round(score * 100.0) / 100.0, correctCount, ps.size() - correctCount, details);
  }

  private GeneratedTest requirePending(Long testId) {
    GeneratedTest test = tests.findById(testId)
        .orElseThrow(() -> new SyllabusException("Test no existe: " + testId, 404));
    if (test.getStatus() != GeneratedTest.Status.PENDING) {
      throw new SyllabusException("Test ya finalizado", 422);
    }
    return test;
  }
}
