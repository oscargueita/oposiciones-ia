package com.examprep.exam;

import jakarta.persistence.*;

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

  public Answer(Long questionId, int option, int correct) {
    this.questionId = questionId;
    this.option = option;
    this.correct = correct;
  }

  public Long getQuestionId() { return questionId; }
  public int getOption() { return option; }
  public void setOption(int option) { this.option = option; }
  public int getCorrect() { return correct; }
  public void setCorrect(int correct) { this.correct = correct; }
}
