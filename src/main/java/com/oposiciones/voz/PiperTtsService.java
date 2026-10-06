package com.oposiciones.voz;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Implementación alternativa con Piper local (red neuronal offline).
 * Se activa con <code>app.tts.motor=piper</code>; requiere binario + modelo
 * es_ES en <code>data/voces/</code> (ver quickstart 002).
 */
@Service
@ConditionalOnProperty(name = "app.tts.motor", havingValue = "piper")
public class PiperTtsService implements TtsService {

  private final String piperBin;
  private final String model;

  public PiperTtsService(@Value("${app.tts.piper-bin:piper}") String piperBin,
      @Value("${app.tts.piper-model:data/voces/es_ES-davefx-medium.onnx}") String model) {
    this.piperBin = piperBin;
    this.model = model;
  }

  @Override
  public Audio sintetizar(String texto) {
    if (texto == null || texto.isBlank()) {
      throw new com.oposiciones.temario.TemarioException("Texto vacío, nada que sintetizar", 422);
    }
    try {
      Path tmp = Files.createTempFile("voz-piper-", ".wav");
      try {
        Process p = new ProcessBuilder(piperBin, "--model", model, "--output_file", tmp.toString())
            .redirectErrorStream(true).start();
        p.getOutputStream().write(texto.getBytes(StandardCharsets.UTF_8));
        p.getOutputStream().close();
        String log = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code != 0) {
          throw new com.oposiciones.temario.TemarioException("Piper falló: " + log, 500);
        }
        byte[] wav = Files.readAllBytes(tmp);
        return new Audio(wav, SayTtsService.WavUtil.duracionSeg(wav), "wav");
      } finally {
        Files.deleteIfExists(tmp);
      }
    } catch (com.oposiciones.temario.TemarioException e) {
      throw e;
    } catch (Exception e) {
      throw new com.oposiciones.temario.TemarioException("No se pudo sintetizar voz: " + e.getMessage(), 500, e);
    }
  }
}
