package com.oposiciones.voz;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progreso_escucha")
public class ProgresoEscucha {

  @Id
  @Column(name = "tema_id")
  private Long temaId;

  @Column(name = "fragmento_id", nullable = false)
  private Long fragmentoId;

  @Column(name = "offset_seg", nullable = false)
  private double offsetSeg;

  @Column(name = "actualizado_en", nullable = false)
  private LocalDateTime actualizadoEn = LocalDateTime.now();

  protected ProgresoEscucha() {}

  public ProgresoEscucha(Long temaId, Long fragmentoId, double offsetSeg) {
    this.temaId = temaId;
    this.fragmentoId = fragmentoId;
    this.offsetSeg = offsetSeg;
  }

  public Long getTemaId() { return temaId; }
  public Long getFragmentoId() { return fragmentoId; }
  public double getOffsetSeg() { return offsetSeg; }
  public void setFragmentoId(Long fragmentoId) { this.fragmentoId = fragmentoId; }
  public void setOffsetSeg(double offsetSeg) { this.offsetSeg = offsetSeg; }
  public void setActualizadoEn(LocalDateTime t) { this.actualizadoEn = t; }
  public LocalDateTime getActualizadoEn() { return actualizadoEn; }
}
