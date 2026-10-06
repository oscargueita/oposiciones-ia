package com.oposiciones.voz;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fragmento_audio")
public class AudioFragmento {

  @Id
  @Column(name = "fragmento_id")
  private Long fragmentoId;

  @Column(name = "duracion_seg", nullable = false)
  private double duracionSeg;

  /** "m4a" (say) o "wav" (piper). */
  @Column(nullable = false)
  private String formato;

  @Column(name = "generado_en", nullable = false)
  private LocalDateTime generadoEn = LocalDateTime.now();

  protected AudioFragmento() {}

  public AudioFragmento(Long fragmentoId, double duracionSeg, String formato) {
    this.fragmentoId = fragmentoId;
    this.duracionSeg = duracionSeg;
    this.formato = formato;
  }

  public Long getFragmentoId() { return fragmentoId; }
  public double getDuracionSeg() { return duracionSeg; }
  public String getFormato() { return formato; }
  public LocalDateTime getGeneradoEn() { return generadoEn; }
}
