package com.oposiciones.voz;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Almacén de WAV por fragmento en `data/audio/{temaId}/{fragmentoId}.wav` (gitignored). */
@Service
public class AudioStore {

  private final Path baseDir;

  public AudioStore(@Value("${app.tts.audio-dir:data/audio}") String baseDir) {
    this.baseDir = Path.of(baseDir);
  }

  public Path ruta(Long temaId, Long fragmentoId, String formato) {
    return baseDir.resolve(String.valueOf(temaId)).resolve(fragmentoId + "." + formato);
  }

  public void guardar(Long temaId, Long fragmentoId, String formato, byte[] datos) {
    try {
      Path p = ruta(temaId, fragmentoId, formato);
      Files.createDirectories(p.getParent());
      Files.write(p, datos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public byte[] leer(Long temaId, Long fragmentoId, String formato) {
    try {
      return Files.readAllBytes(ruta(temaId, fragmentoId, formato));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public boolean existe(Long temaId, Long fragmentoId, String formato) {
    return Files.isRegularFile(ruta(temaId, fragmentoId, formato));
  }

  public void borrarTema(Long temaId) {
    try {
      Path dir = baseDir.resolve(String.valueOf(temaId));
      if (Files.isDirectory(dir)) {
        try (var s = Files.list(dir)) {
          for (Path p : s.toList()) Files.deleteIfExists(p);
        }
        Files.deleteIfExists(dir);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
