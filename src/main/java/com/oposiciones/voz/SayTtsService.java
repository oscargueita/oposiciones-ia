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
      Path tmp = Files.createTempFile("voz-", ".wav");
      try {
        Process p = new ProcessBuilder("say", "-v", voice, "-o", tmp.toString(),
            "--file-format=WAVE", "--data-format=LEI16@22050", texto)
            .redirectErrorStream(true).start();
        String log = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code != 0) {
          throw new com.oposiciones.temario.TemarioException("Motor de voz falló: " + log, 500);
        }
        byte[] wav = Files.readAllBytes(tmp);
        return new Audio(wav, WavUtil.duracionSeg(wav));
      } finally {
        Files.deleteIfExists(tmp);
      }
    } catch (com.oposiciones.temario.TemarioException e) {
      throw e;
    } catch (Exception e) {
      throw new com.oposiciones.temario.TemarioException("No se pudo sintetizar voz: " + e.getMessage(), 500, e);
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
