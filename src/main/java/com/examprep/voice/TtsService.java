package com.examprep.voice;

/**
 * Decoupled voice engine (constitution V). All synthesis goes through here;
 * the domain never invokes binaries or external services directly.
 */
public interface TtsService {

  /** Synthesized audio. `extension` is "m4a" (say/AAC) or "wav" (piper). */
  Audio synthesize(String text);

  record Audio(byte[] data, double durationSec, String extension) {}
}
