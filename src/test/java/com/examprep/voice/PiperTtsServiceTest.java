package com.examprep.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Tests the real Piper if installed (binary + es_ES model).
 * Skipped (does not fail) on machines without Piper: the test gate stays green.
 */
class PiperTtsServiceTest {

  @Test
  void sintetizaConPiperReal() {
    Path model = Path.of("data/voces/es_ES-davefx-medium.onnx");
    boolean piperOk;
    try {
      piperOk = new ProcessBuilder("sh", "-c", "command -v piper").start().waitFor() == 0;
    } catch (Exception e) {
      piperOk = false;
    }
    assumeTrue(piperOk && Files.isRegularFile(model), "Piper o modelo es_ES no instalados");
    var svc = new PiperTtsService("piper", model.toString());
    var audio = svc.synthesize("Hola, buenos días.");
    assertThat(audio.data()).isNotEmpty();
    assertThat(audio.durationSec()).isPositive();
  }
}
