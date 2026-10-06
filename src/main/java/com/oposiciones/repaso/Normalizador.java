package com.oposiciones.repaso;

import java.text.Normalizer;

/** Minúsculas + sin tildes para comparar consulta y texto (búsqueda insensible). */
public final class Normalizador {
  private Normalizador() {}

  public static String normalizar(String s) {
    if (s == null) return "";
    String n = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}", " ");
  }
}
