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

  @Column(name = "generado_en", nullable = false)
  private LocalDateTime generadoEn = LocalDateTime.now();

  protected AudioFragmento() {}

  public AudioFragmento(Long fragmentoId, double duracionSeg) {
    this.fragmentoId = fragmentoId;
    this.duracionSeg = duracionSeg;
  }

  public Long getFragmentoId() { return fragmentoId; }
  public double getDuracionSeg() { return duracionSeg; }
  public LocalDateTime getGeneradoEn() { return generadoEn; }
}
