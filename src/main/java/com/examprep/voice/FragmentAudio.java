package com.examprep.voice;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fragmento_audio")
public class FragmentAudio {

  @Id
  @Column(name = "fragmento_id")
  private Long fragmentId;

  @Column(name = "duracion_seg", nullable = false)
  private double durationSec;

  /** "m4a" (say) or "wav" (piper). */
  @Column(name = "formato", nullable = false)
  private String format;

  @Column(name = "generado_en", nullable = false)
  private LocalDateTime generatedAt = LocalDateTime.now();

  protected FragmentAudio() {}

  public FragmentAudio(Long fragmentId, double durationSec, String format) {
    this.fragmentId = fragmentId;
    this.durationSec = durationSec;
    this.format = format;
  }

  public Long getFragmentId() { return fragmentId; }
  public double getDurationSec() { return durationSec; }
  public String getFormat() { return format; }
  public LocalDateTime getGeneratedAt() { return generatedAt; }
}
