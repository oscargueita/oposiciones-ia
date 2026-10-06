package com.oposiciones.examen;

import jakarta.persistence.*;

@Entity
@Table(name = "pregunta")
public class Pregunta {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "test_id", nullable = false)
  private Long testId;

  @Column(nullable = false)
  private int orden;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String enunciado;

  /** JSON array de exactamente 4 opciones. */
  @Column(nullable = false, columnDefinition = "TEXT")
  private String opciones;

  @Column(nullable = false)
  private int correcta;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String explicacion;

  @Column(name = "cita_tema_id", nullable = false)
  private Long citaTemaId;

  @Column(name = "cita_fragmento_id", nullable = false)
  private Long citaFragmentoId;

  @Column(name = "cita_pagina", nullable = false)
  private int citaPagina;

  protected Pregunta() {}

  public Pregunta(Long testId, int orden, String enunciado, String opciones, int correcta,
      String explicacion, Long citaTemaId, Long citaFragmentoId, int citaPagina) {
    this.testId = testId;
    this.orden = orden;
    this.enunciado = enunciado;
    this.opciones = opciones;
    this.correcta = correcta;
    this.explicacion = explicacion;
    this.citaTemaId = citaTemaId;
    this.citaFragmentoId = citaFragmentoId;
    this.citaPagina = citaPagina;
  }

  public Long getId() { return id; }
  public Long getTestId() { return testId; }
  public int getOrden() { return orden; }
  public String getEnunciado() { return enunciado; }
  public String getOpciones() { return opciones; }
  public int getCorrecta() { return correcta; }
  public String getExplicacion() { return explicacion; }
  public Long getCitaTemaId() { return citaTemaId; }
  public Long getCitaFragmentoId() { return citaFragmentoId; }
  public int getCitaPagina() { return citaPagina; }
}
