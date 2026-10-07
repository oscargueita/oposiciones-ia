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

  @Column(name = "tema_id", nullable = false)
  private Long topicId;

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
    this.topicId = topicId;
    this.difficulty = difficulty;
    this.questionCount = questionCount;
  }

  public Long getId() { return id; }
  public Long getTopicId() { return topicId; }
  public Difficulty getDifficulty() { return difficulty; }
  public int getNumPreguntas() { return questionCount; }
  public void setNumPreguntas(int n) { this.questionCount = n; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
