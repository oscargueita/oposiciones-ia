package com.examprep.voice;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_escucha")
public class ListeningProgress {

  @Id
  @Column(name = "tema_id")
  private Long topicId;

  @Column(name = "fragmento_id", nullable = false)
  private Long fragmentId;

  @Column(name = "offset_seg", nullable = false)
  private double offsetSec;

  @Column(name = "actualizado_en", nullable = false)
  private LocalDateTime updatedAt = LocalDateTime.now();

  protected ListeningProgress() {}

  public ListeningProgress(Long topicId, Long fragmentId, double offsetSec) {
    this.topicId = topicId;
    this.fragmentId = fragmentId;
    this.offsetSec = offsetSec;
  }

  public Long getTopicId() { return topicId; }
  public Long getFragmentId() { return fragmentId; }
  public double getOffsetSec() { return offsetSec; }
  public void setFragmentId(Long fragmentId) { this.fragmentId = fragmentId; }
  public void setOffsetSec(double offsetSec) { this.offsetSec = offsetSec; }
  public void setUpdatedAt(LocalDateTime t) { this.updatedAt = t; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
