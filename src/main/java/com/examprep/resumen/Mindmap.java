package com.examprep.resumen;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mapa")
public class Mindmap {

  @Id
  @Column(name = "tema_id")
  private Long topicId;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String mermaid;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected Mindmap() {}

  public Mindmap(Long topicId, String mermaid) {
    if (topicId == null) throw new IllegalArgumentException("Tema obligatorio");
    if (mermaid == null || mermaid.isBlank()) throw new IllegalArgumentException("Mermaid no vacío");
    this.topicId = topicId;
    this.mermaid = mermaid;
  }

  public Long getTopicId() { return topicId; }
  public String getMermaid() { return mermaid; }
}
