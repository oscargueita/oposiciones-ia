package com.oposiciones.temario;

import java.security.MessageDigest;

public final class Sha256 {
  private Sha256() {}

  public static String of(byte[] bytes) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(bytes);
      StringBuilder sb = new StringBuilder(64);
      for (byte b : digest) sb.append(String.format("%02x", b));
      return sb.toString();
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 no disponible", e);
    }
  }
}
