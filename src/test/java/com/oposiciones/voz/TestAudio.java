package com.oposiciones.voz;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

/** WAV PCM 22050Hz mono 16-bit sintético (silencio) para tests, sin motor real. */
public final class TestAudio {
  private TestAudio() {}

  public static byte[] wav(double segundos) {
    return wavConChunkExtra(segundos, null);
  }

  /** WAV con un chunk extra (p. ej. FLLR como escribe `say`) antes de data. */
  public static byte[] wavConChunkExtra(double segundos, String extraId) {
    int rate = 22050;
    int samples = (int) (segundos * rate);
    byte[] extra = extraId == null ? new byte[0] : chunk(extraId, 100);
    ByteBuffer b = ByteBuffer.allocate(44 + extra.length + samples * 2).order(ByteOrder.LITTLE_ENDIAN);
    b.put("RIFF".getBytes());
    b.putInt(36 + extra.length + samples * 2);
    b.put("WAVEfmt ".getBytes());
    b.putInt(16);
    b.putShort((short) 1);
    b.putShort((short) 1);
    b.putInt(rate);
    b.putInt(rate * 2);
    b.putShort((short) 2);
    b.putShort((short) 16);
    b.put(extra);
    b.put("data".getBytes());
    b.putInt(samples * 2);
    return b.array();
  }

  private static byte[] chunk(String id, int size) {
    ByteBuffer b = ByteBuffer.allocate(8 + size).order(ByteOrder.LITTLE_ENDIAN);
    b.put(id.getBytes());
    b.putInt(size);
    return b.array();
  }

  public static void limpiar(Path dir) {
    try {
      if (Files.isDirectory(dir)) {
        try (var s = Files.walk(dir)) {
          for (Path p : s.sorted(Comparator.reverseOrder()).toList()) Files.deleteIfExists(p);
        }
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
