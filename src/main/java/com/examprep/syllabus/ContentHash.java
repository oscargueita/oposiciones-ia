package com.examprep.syllabus;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

/** SHA-256 del contenido de un PDF. Value object: valida su forma al crearse. */
@Embeddable
public class ContentHash {

  @Column(name = "content_sha256", nullable = false, unique = true, length = 64)
  private String value;

  protected ContentHash() {}

  private ContentHash(String value) {
    this.value = value;
  }

  public static ContentHash of(String value) {
    if (value == null || !value.matches("[0-9a-f]{64}")) {
      throw new IllegalArgumentException("SHA-256 inválido");
    }
    return new ContentHash(value);
  }

  public static ContentHash ofBytes(byte[] bytes) {
    try {
      byte[] digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
      StringBuilder sb = new StringBuilder(64);
      for (byte b : digest) sb.append(String.format("%02x", b));
      return new ContentHash(sb.toString());
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    }
  }

  public String value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    return o instanceof ContentHash other && value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
