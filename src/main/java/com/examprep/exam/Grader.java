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
    if (!p.belongsTo(test.getId())) {
      throw new SyllabusException("La pregunta no pertenece al test", 422);
    }
    final Answer given;
    try {
      given = new Answer(p, option);
    } catch (IllegalArgumentException e) {
      throw new SyllabusException("Opción 0-3", 422);
    }
    answers.findById(questionId).ifPresent(answers::delete);
    answers.save(given);
    return new Feedback(given.isCorrect(), p.getCorrectIndex(), p.getExplanation(),
        p.getCitedTopicId(), p.getCitedFragmentId(), p.getCitedPage());
  }

  @Transactional
  public Grade finish(Long testId) {
    GeneratedTest test = requirePending(testId);
    test.finish();
    return calculate(testId);
  }

  @Transactional(readOnly = true)
  public Grade summary(Long testId) {
    if (!tests.existsById(testId)) {
      throw new SyllabusException("Test no existe: " + testId, 404);
    }
    return calculate(testId);
  }

  private Grade calculate(Long testId) {
    GeneratedTest test = tests.findById(testId)
        .orElseThrow(() -> new SyllabusException("Test no existe: " + testId, 404));
    List<Question> ps = questions.findByTestIdOrderBySequenceAsc(testId);
    var byQuestion = new java.util.HashMap<Long, Answer>();
    for (Question p : ps) {
      answers.findById(p.getId()).ifPresent(a -> byQuestion.put(p.getId(), a));
    }
    Score score = test.grade(ps, byQuestion);
    var details = new java.util.ArrayList<Detail>();
    for (Question p : ps) {
      var r = answers.findById(p.getId());
      Integer selected = r.map(Answer::getOption).orElse(null);
      boolean ok = r.map(Answer::isCorrect).orElse(false);
      details.add(new Detail(p.getId(), p.getStatement(), selected, p.getCorrectIndex(), ok,
          p.getExplanation()));
    }
    return new Grade(score.value(), score.correctCount(), score.wrongCount(), details);
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
