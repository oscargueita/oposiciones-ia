package com.examprep.exam;

import com.examprep.syllabus.SyllabusException;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_generado")
public class GeneratedTest {

  public enum Status { PENDING, GRADED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tema_id")
  private Long topicId;

  /** 'TEMA' or mixed scope label, e.g. 'Mixto (3 temas)', 'Todos (83 temas)'. */
  @Column(name = "alcance", nullable = false)
  private String scope = "TEMA";

  @Enumerated(EnumType.STRING)
  @Column(name = "dificultad", nullable = false, length = 10)
  private Difficulty difficulty;

  @Column(name = "num_preguntas", nullable = false)
  private int questionCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 10)
  private Status status = Status.PENDING;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected GeneratedTest() {}

  public GeneratedTest(Long topicId, Difficulty difficulty, int questionCount) {
    this(topicId, "TEMA", difficulty, questionCount);
  }

  public GeneratedTest(Long topicId, String scope, Difficulty difficulty, int questionCount) {
    this.topicId = topicId;
    this.scope = scope;
    this.difficulty = difficulty;
    this.questionCount = questionCount;
  }

  public Long getId() { return id; }
  public Long getTopicId() { return topicId; }
  public String getScope() { return scope; }
  public Difficulty getDifficulty() { return difficulty; }
  public int getQuestionCount() { return questionCount; }
  public Status getStatus() { return status; }
  public void adjustQuestionCount(int n) {
    if (n < 1) throw new IllegalArgumentException("Al menos 1 pregunta");
    this.questionCount = n;
  }

  public void finish() {
    if (status != Status.PENDING) {
      throw new SyllabusException("Test ya finalizado", 422);
    }
    this.status = Status.GRADED;
  }

  public boolean isGraded() {
    return status == Status.GRADED;
  }

  /** Calcula la nota desde las respuestas dadas (ausentes = fallo). */
  public Score grade(java.util.List<Question> questions,
      java.util.Map<Long, Answer> answersByQuestion) {
    int correct = 0;
    for (Question q : questions) {
      if (!q.belongsTo(this.id)) {
        throw new IllegalArgumentException("Pregunta de otro test");
      }
      Answer a = answersByQuestion.get(q.getId());
      if (a != null && a.isCorrect()) correct++;
    }
    return Score.of(correct, questions.size());
  }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
