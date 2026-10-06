package com.oposiciones.examen;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_generado")
public class TestGenerado {

  public enum Estado { PENDIENTE, CORREGIDO }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "tema_id", nullable = false)
  private Long temaId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Dificultad dificultad;

  @Column(name = "num_preguntas", nullable = false)
  private int numPreguntas;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Estado estado = Estado.PENDIENTE;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn = LocalDateTime.now();

  protected TestGenerado() {}

  public TestGenerado(Long temaId, Dificultad dificultad, int numPreguntas) {
    this.temaId = temaId;
    this.dificultad = dificultad;
    this.numPreguntas = numPreguntas;
  }

  public Long getId() { return id; }
  public Long getTemaId() { return temaId; }
  public Dificultad getDificultad() { return dificultad; }
  public int getNumPreguntas() { return numPreguntas; }
  public void setNumPreguntas(int n) { this.numPreguntas = n; }
  public Estado getEstado() { return estado; }
  public void setEstado(Estado estado) { this.estado = estado; }
  public LocalDateTime getCreadoEn() { return creadoEn; }
}
