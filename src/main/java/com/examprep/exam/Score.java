package com.examprep.exam;

/** Nota sobre 10 con redondeo a 2 decimales. Value object inmutable. */
public record Score(double value, int correctCount, int wrongCount) {

  public static Score of(int correctCount, int total) {
    if (total <= 0) return new Score(0, 0, 0);
    double value = Math.round(1000.0 * correctCount / total) / 100.0;
    return new Score(value, correctCount, total - correctCount);
  }
}
