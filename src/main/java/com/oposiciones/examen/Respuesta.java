package com.oposiciones.examen;

import jakarta.persistence.*;

@Entity
@Table(name = "respuesta")
public class Respuesta {

  @Id
  @Column(name = "pregunta_id")
  private Long preguntaId;

  @Column(nullable = false)
  private int opcion;

  @Column(nullable = false)
  private int acierto;

  protected Respuesta() {}

  public Respuesta(Long preguntaId, int opcion, int acierto) {
    this.preguntaId = preguntaId;
    this.opcion = opcion;
    this.acierto = acierto;
  }

  public Long getPreguntaId() { return preguntaId; }
  public int getOpcion() { return opcion; }
  public void setOpcion(int opcion) { this.opcion = opcion; }
  public int getAcierto() { return acierto; }
  public void setAcierto(int acierto) { this.acierto = acierto; }
}
