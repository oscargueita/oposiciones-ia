package com.examprep.voice;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Alternative implementation with local Piper (offline neural network).
 * Enabled with <code>app.tts.motor=piper</code>; requires binary + es_ES
 * model in <code>data/voces/</code> (see quickstart 002).
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
  public Audio synthesize(String text) {
    if (text == null || text.isBlank()) {
      throw new com.examprep.syllabus.SyllabusException("Texto vacío, nada que synthesize", 422);
    }
    try {
      Path tmp = Files.createTempFile("voz-piper-", ".wav");
      try {
        Process p = new ProcessBuilder(piperBin, "--model", model, "--output_file", tmp.toString())
            .redirectErrorStream(true).start();
        p.getOutputStream().write(text.getBytes(StandardCharsets.UTF_8));
        p.getOutputStream().close();
        String log = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code != 0) {
          throw new com.examprep.syllabus.SyllabusException("Piper falló: " + log, 500);
        }
        byte[] wav = Files.readAllBytes(tmp);
        return new Audio(wav, SayTtsService.WavUtil.durationSec(wav), "wav");
      } finally {
        Files.deleteIfExists(tmp);
      }
    } catch (com.examprep.syllabus.SyllabusException e) {
      throw e;
    } catch (Exception e) {
      throw new com.examprep.syllabus.SyllabusException("No se pudo synthesize voz: " + e.getMessage(), 500, e);
    }
  }
}
