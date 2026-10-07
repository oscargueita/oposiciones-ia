package com.examprep.resumen;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chuleta")
public class Cheatsheet {

  @Id
  @Column(name = "tema_id")
  private Long topicId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String markdown;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected Cheatsheet() {}

  public Cheatsheet(Long topicId, String markdown) {
    this.topicId = topicId;
    this.markdown = markdown;
  }

  public Long getTopicId() { return topicId; }
  public String getMarkdown() { return markdown; }
}
