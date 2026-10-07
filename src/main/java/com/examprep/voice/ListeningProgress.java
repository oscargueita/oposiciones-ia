package com.examprep.voice;

import com.examprep.syllabus.SyllabusException;
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
    if (topicId == null) throw new IllegalArgumentException("Tema obligatorio");
    this.topicId = topicId;
    moveTo(fragmentId, offsetSec, Double.MAX_VALUE);
  }

  /** Mueve el punto de escucha validando contra la duración del audio. */
  public void moveTo(Long fragmentId, double offsetSec, double audioDurationSec) {
    if (fragmentId == null) throw new IllegalArgumentException("Fragmento obligatorio");
    if (offsetSec < 0 || offsetSec > audioDurationSec) {
      throw new SyllabusException(
          "Offset fuera del audio (0-" + audioDurationSec + "s)", 422);
    }
    this.fragmentId = fragmentId;
    this.offsetSec = offsetSec;
    this.updatedAt = LocalDateTime.now();
  }

  public Long getTopicId() { return topicId; }
  public Long getFragmentId() { return fragmentId; }
  public double getOffsetSec() { return offsetSec; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
