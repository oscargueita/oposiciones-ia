package com.oposiciones.voz;

/**
 * Motor de voz desacoplado (constitution V). Toda la síntesis pasa por aquí;
 * el dominio nunca invoca binarios ni servicios externos directamente.
 */
public interface TtsService {

  /** Audio sintetizado. `extension` es "m4a" (say/AAC) o "wav" (piper). */
  Audio sintetizar(String texto);

  record Audio(byte[] datos, double duracionSeg, String extension) {}
}
