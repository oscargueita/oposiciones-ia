package com.examprep.exam;

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

  /** 'TEMA' o etiqueta del alcance mixto, p. ej. 'Mixto (3 temas)', 'Todos (83 temas)'. */
  @Column(nullable = false)
  private String alcance = "TEMA";

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

  public GeneratedTest(Long topicId, String alcance, Difficulty difficulty, int questionCount) {
    this.topicId = topicId;
    this.alcance = alcance;
    this.difficulty = difficulty;
    this.questionCount = questionCount;
  }

  public Long getId() { return id; }
  public Long getTopicId() { return topicId; }
  public String getAlcance() { return alcance; }
  public Difficulty getDifficulty() { return difficulty; }
  public int getQuestionCount() { return questionCount; }
  public void setQuestionCount(int n) { this.questionCount = n; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
