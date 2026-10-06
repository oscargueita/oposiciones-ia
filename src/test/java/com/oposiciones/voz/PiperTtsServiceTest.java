package com.oposiciones.voz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/**
 * Prueba el Piper real si está instalado (binario + modelo es_ES).
 * Se omite (no falla) en máquinas sin Piper: el gate de tests sigue verde.
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
    var audio = svc.sintetizar("Hola, buenos días.");
    assertThat(audio.wav()).isNotEmpty();
    assertThat(audio.duracionSeg()).isPositive();
  }
}
