package com.examprep.voice;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Per-fragment WAV store in `data/audio/{topicId}/{fragmentId}.wav` (gitignored). */
@Service
public class AudioStore {

  private final Path baseDir;

  public AudioStore(@Value("${app.tts.audio-dir:data/audio}") String baseDir) {
    this.baseDir = Path.of(baseDir);
  }

  public Path ruta(Long topicId, Long fragmentId, String format) {
    return baseDir.resolve(String.valueOf(topicId)).resolve(fragmentId + "." + format);
  }

  public void save(Long topicId, Long fragmentId, String format, byte[] data) {
    try {
      Path p = ruta(topicId, fragmentId, format);
      Files.createDirectories(p.getParent());
      Files.write(p, data);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public byte[] read(Long topicId, Long fragmentId, String format) {
    try {
      return Files.readAllBytes(ruta(topicId, fragmentId, format));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public boolean exists(Long topicId, Long fragmentId, String format) {
    return Files.isRegularFile(ruta(topicId, fragmentId, format));
  }

  public void deleteTopic(Long topicId) {
    try {
      Path dir = baseDir.resolve(String.valueOf(topicId));
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
