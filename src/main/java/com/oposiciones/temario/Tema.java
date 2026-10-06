package com.oposiciones.temario;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "temas")
public class Tema {

  public enum Estado { PROCESANDO, LISTO, ERROR }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String titulo;

  @Column(name = "origen_nombre", nullable = false)
  private String origenNombre;

  @Column(name = "content_sha256", nullable = false, unique = true, length = 64)
  private String contentSha256;

  @Column(name = "num_paginas", nullable = false)
  private int numPaginas;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 12)
  private Estado estado = Estado.PROCESANDO;

  @Column(name = "mensaje_error")
  private String mensajeError;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime creadoEn = LocalDateTime.now();

  protected Tema() {}

  public Tema(String titulo, String origenNombre, String contentSha256, int numPaginas) {
    this.titulo = titulo;
    this.origenNombre = origenNombre;
    this.contentSha256 = contentSha256;
    this.numPaginas = numPaginas;
  }

  public Long getId() { return id; }
  public String getTitulo() { return titulo; }
  public void setTitulo(String titulo) { this.titulo = titulo; }
  public String getOrigenNombre() { return origenNombre; }
  public String getContentSha256() { return contentSha256; }
  public void setContentSha256(String sha) { this.contentSha256 = sha; }
  public int getNumPaginas() { return numPaginas; }
  public void setNumPaginas(int n) { this.numPaginas = n; }
  public Estado getEstado() { return estado; }
  public void setEstado(Estado estado) { this.estado = estado; }
  public String getMensajeError() { return mensajeError; }
  public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
  public LocalDateTime getCreadoEn() { return creadoEn; }
}
