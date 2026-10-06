package com.oposiciones.voz;

/**
 * Motor de voz desacoplado (constitution V). Toda la síntesis pasa por aquí;
 * el dominio nunca invoca binarios ni servicios externos directamente.
 */
public interface TtsService {

  /** Audio WAV 22050Hz mono 16-bit del texto dado. */
  Audio sintetizar(String texto);

  record Audio(byte[] wav, double duracionSeg) {}
}
