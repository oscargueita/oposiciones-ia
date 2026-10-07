package com.examprep.review;

import java.text.Normalizer;

/** Lowercase + no accents to compare query and text (insensitive search). */
public final class TextNormalizer {
  private TextNormalizer() {}

  public static String normalize(String s) {
    if (s == null) return "";
    String n = Normalizer.normalize(s.toLowerCase(), Normalizer.Form.NFD);
    return n.replaceAll("\\p{M}", " ");
  }
}
