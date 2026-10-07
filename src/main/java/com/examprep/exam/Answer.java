package com.examprep.exam;

import jakarta.persistence.*;

/**
 * Answer given to a question. Immutable once created: re-answering
 * creates a new instance (upsert at repository level).
 */
@Entity
@Table(name = "respuesta")
public class Answer {

  @Id
  @Column(name = "pregunta_id")
  private Long questionId;

  @Column(name = "opcion", nullable = false)
  private int option;

  @Column(name = "acierto", nullable = false)
  private int correct;

  protected Answer() {}

  /** Creates the answer to {@code question}, grading it immediately. */
  public Answer(Question question, int option) {
    if (question == null) throw new IllegalArgumentException("Pregunta obligatoria");
    if (option < 0 || option > 3) throw new IllegalArgumentException("Opción 0-3");
    this.questionId = question.getId();
    this.option = option;
    this.correct = option == question.getCorrectIndex() ? 1 : 0;
  }

  public boolean isCorrect() {
    return correct == 1;
  }

  public Long getQuestionId() { return questionId; }
  public int getOption() { return option; }
  public int getCorrect() { return correct; }
}
