package com.oposiciones.voz;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** Implementación v1 con `say` de macOS (100% local, sin descargas). */
@Service
@Primary
public class SayTtsService implements TtsService {

  private final String voice;

  public SayTtsService(@Value("${app.tts.say-voice:Mónica}") String voice) {
    this.voice = voice;
  }

  @Override
  public Audio sintetizar(String texto) {
    if (texto == null || texto.isBlank()) {
      throw new com.oposiciones.temario.TemarioException("Texto vacío, nada que sintetizar", 422);
    }
    try {
      Path tmp = Files.createTempFile("voz-", ".m4a");
      try {
        // M4A/AAC comprimido (~36kbps, ~10x menos que WAV/PCM); la extensión manda el contenedor
        Process p = new ProcessBuilder("say", "-v", voice, "-o", tmp.toString(),
            "--file-format=m4af", "--data-format=aac", texto)
            .redirectErrorStream(true).start();
        String log = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code != 0) {
          throw new com.oposiciones.temario.TemarioException("Motor de voz falló: " + log, 500);
        }
        byte[] audio = Files.readAllBytes(tmp);
        return new Audio(audio, M4aUtil.duracionSeg(tmp), "m4a");
      } finally {
        Files.deleteIfExists(tmp);
      }
    } catch (com.oposiciones.temario.TemarioException e) {
      throw e;
    } catch (Exception e) {
      throw new com.oposiciones.temario.TemarioException("No se pudo sintetizar voz: " + e.getMessage(), 500, e);
    }
  }

  /** Duración en segundos vía `afinfo` (macOS). */
  static final class M4aUtil {
    static double duracionSeg(Path m4a) throws Exception {
      Process p = new ProcessBuilder("afinfo", m4a.toString())
          .redirectErrorStream(true).start();
      String out = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
      p.waitFor();
      var m = java.util.regex.Pattern.compile("estimated duration: ([0-9.]+) sec")
          .matcher(out);
      if (m.find()) return Double.parseDouble(m.group(1));
      throw new IllegalArgumentException("afinfo sin duración: " + out.lines().findFirst().orElse("?"));
    }
  }

  /** Duración en segundos desde la cabecera WAV (recorre chunks: `say` inserta FLLR). */
  static final class WavUtil {
    static double duracionSeg(byte[] wav) {
      if (wav.length < 44) throw new IllegalArgumentException("WAV demasiado corto");
      ByteBuffer b = ByteBuffer.wrap(wav).order(ByteOrder.LITTLE_ENDIAN);
      if (b.getInt(0) != 0x46464952) throw new IllegalArgumentException("No es RIFF");
      int byteRate = b.getInt(28);
      int pos = 12;
      while (pos + 8 <= wav.length) {
        int id = b.getInt(pos);
        int size = b.getInt(pos + 4);
        if (id == 0x61746164) { // "data"
          if (byteRate <= 0) throw new IllegalArgumentException("WAV sin byte rate");
          return (double) size / byteRate;
        }
        pos += 8 + size;
      }
      throw new IllegalArgumentException("WAV sin chunk data");
    }
  }
}
