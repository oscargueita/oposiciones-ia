package com.examprep.voice;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/** V1 implementation with macOS `say` (100% local, no downloads). */
@Service
@Primary
public class SayTtsService implements TtsService {

  private final String voice;

  public SayTtsService(@Value("${app.tts.say-voice:Mónica}") String voice) {
    this.voice = voice;
  }

  @Override
  public Audio synthesize(String text) {
    if (text == null || text.isBlank()) {
      throw new com.examprep.syllabus.SyllabusException("Texto vacío, nada que synthesize", 422);
    }
    try {
      Path tmp = Files.createTempFile("voz-", ".m4a");
      try {
        // Compressed M4A/AAC (~36kbps, ~10x smaller than WAV/PCM); the extension selects the container
        Process p = new ProcessBuilder("say", "-v", voice, "-o", tmp.toString(),
            "--file-format=m4af", "--data-format=aac", text)
            .redirectErrorStream(true).start();
        String log = new String(p.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        int code = p.waitFor();
        if (code != 0) {
          throw new com.examprep.syllabus.SyllabusException("Motor de voz falló: " + log, 500);
        }
        byte[] audio = Files.readAllBytes(tmp);
        return new Audio(audio, M4aUtil.durationSec(tmp), "m4a");
      } finally {
        Files.deleteIfExists(tmp);
      }
    } catch (com.examprep.syllabus.SyllabusException e) {
      throw e;
    } catch (Exception e) {
      throw new com.examprep.syllabus.SyllabusException("No se pudo synthesize voz: " + e.getMessage(), 500, e);
    }
  }

  /** Duration in seconds via `afinfo` (macOS). */
  static final class M4aUtil {
    static double durationSec(Path m4a) throws Exception {
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

  /** Duration in seconds from the WAV header (walks chunks: `say` inserts FLLR). */
  static final class WavUtil {
    static double durationSec(byte[] wav) {
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
