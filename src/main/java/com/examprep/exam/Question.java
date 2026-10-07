package com.examprep.exam;

import jakarta.persistence.*;

@Entity
@Table(name = "pregunta")
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "test_id", nullable = false)
  private Long testId;

  @Column(name = "orden", nullable = false)
  private int sequence;

  @Column(name = "enunciado", nullable = false, columnDefinition = "TEXT")
  private String statement;

  /** JSON array of exactly 4 options. */
  @Column(name = "opciones", nullable = false, columnDefinition = "TEXT")
  private String options;

  @Column(name = "correcta", nullable = false)
  private int correctIndex;

  @Column(name = "explicacion", nullable = false, columnDefinition = "TEXT")
  private String explanation;

  @Column(name = "cita_tema_id", nullable = false)
  private Long citedTopicId;

  @Column(name = "cita_fragmento_id", nullable = false)
  private Long citedFragmentId;

  @Column(name = "cita_pagina", nullable = false)
  private int citedPage;

  protected Question() {}

  public Question(Long testId, int sequence, String statement, String options, int correctIndex,
      String explanation, Long citedTopicId, Long citedFragmentId, int citedPage) {
    if (testId == null) throw new IllegalArgumentException("Test obligatorio");
    if (sequence < 0) throw new IllegalArgumentException("Orden >= 0");
    if (statement == null || statement.isBlank()) throw new IllegalArgumentException("Enunciado no vacío");
    if (options == null || options.isBlank()) throw new IllegalArgumentException("Opciones no vacías");
    if (correctIndex < 0 || correctIndex > 3) throw new IllegalArgumentException("Correcta 0-3");
    if (explanation == null || explanation.isBlank()) throw new IllegalArgumentException("Explicación no vacía");
    if (citedPage < 1) throw new IllegalArgumentException("Página >= 1");
    this.testId = testId;
    this.sequence = sequence;
    this.statement = statement;
    this.options = options;
    this.correctIndex = correctIndex;
    this.explanation = explanation;
    this.citedTopicId = citedTopicId;
    this.citedFragmentId = citedFragmentId;
    this.citedPage = citedPage;
  }

  public Long getId() { return id; }
  public Long getTestId() { return testId; }

  public boolean belongsTo(Long testId) {
    return this.testId.equals(testId);
  }
  public int getSequence() { return sequence; }
  public String getStatement() { return statement; }
  public String getOptions() { return options; }
  public int getCorrectIndex() { return correctIndex; }
  public String getExplanation() { return explanation; }
  public Long getCitedTopicId() { return citedTopicId; }
  public Long getCitedFragmentId() { return citedFragmentId; }
  public int getCitedPage() { return citedPage; }
}
