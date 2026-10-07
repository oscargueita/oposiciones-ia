package com.examprep.syllabus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Aggregate root: un tema del temario (1 PDF = 1 tema).
 * Protege sus invariantes: título no vacío, páginas > 0,
 * y transiciones de estado válidas. Sin setters públicos.
 */
@Entity
@Table(name = "temas")
public class Topic {

  public enum Status { PROCESSING, READY, FAILED }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "titulo", nullable = false)
  private String title;

  @Column(name = "origen_nombre", nullable = false)
  private String sourceName;

  @Embedded
  private ContentHash contentHash;

  @Column(name = "num_paginas", nullable = false)
  private int pageCount;

  @Enumerated(EnumType.STRING)
  @Column(name = "estado", nullable = false, length = 12)
  private Status status = Status.PROCESSING;

  @Column(name = "mensaje_error")
  private String errorMessage;

  @Column(name = "creado_en", nullable = false)
  private LocalDateTime createdAt = LocalDateTime.now();

  protected Topic() {}

  public Topic(String title, String sourceName, ContentHash contentHash, int pageCount) {
    rename(title);
    if (sourceName == null || sourceName.isBlank()) {
      throw new IllegalArgumentException("Origen vacío");
    }
    if (pageCount <= 0) throw new IllegalArgumentException("Páginas > 0");
    this.sourceName = sourceName;
    this.contentHash = contentHash;
    this.pageCount = pageCount;
  }

  /** Cambia el título editable (se conserva en reemplazos). */
  public void rename(String title) {
    if (title == null || title.isBlank()) {
      throw new SyllabusException("El título no puede estar vacío", 422);
    }
    this.title = title.strip();
  }

  /** Sustituye el contenido manteniendo id y título. */
  public void beginReplacement(ContentHash newHash, int newPageCount) {
    if (newHash.equals(this.contentHash)) {
      throw new SyllabusException("El PDF es idéntico al actual, nada que reemplazar", 422);
    }
    if (newPageCount <= 0) throw new IllegalArgumentException("Páginas > 0");
    this.contentHash = newHash;
    this.pageCount = newPageCount;
    this.status = Status.PROCESSING;
    this.errorMessage = null;
  }

  public void markReady() {
    this.status = Status.READY;
    this.errorMessage = null;
  }

  public void markFailed(String reason) {
    this.status = Status.FAILED;
    this.errorMessage = reason;
  }

  public boolean isReady() {
    return status == Status.READY;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getSourceName() { return sourceName; }
  public ContentHash getContentHash() { return contentHash; }
  public int getPageCount() { return pageCount; }
  public Status getStatus() { return status; }
  public String getErrorMessage() { return errorMessage; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
