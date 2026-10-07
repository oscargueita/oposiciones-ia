package com.examprep.syllabus;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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

  @Column(name = "content_sha256", nullable = false, unique = true, length = 64)
  private String contentSha256;

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

  public Topic(String title, String sourceName, String contentSha256, int pageCount) {
    this.title = title;
    this.sourceName = sourceName;
    this.contentSha256 = contentSha256;
    this.pageCount = pageCount;
  }

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public String getSourceName() { return sourceName; }
  public String getContentSha256() { return contentSha256; }
  public void setContentSha256(String sha) { this.contentSha256 = sha; }
  public int getPageCount() { return pageCount; }
  public void setPageCount(int n) { this.pageCount = n; }
  public Status getStatus() { return status; }
  public void setStatus(Status status) { this.status = status; }
  public String getErrorMessage() { return errorMessage; }
  public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
  public LocalDateTime getCreatedAt() { return createdAt; }
}
