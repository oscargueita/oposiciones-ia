package com.oposiciones.temario;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

/**
 * Embeddings con Ollama (nomic-embed-text) + búsqueda por coseno en memoria.
 * Vectores normalizados: coseno = producto escalar. Persistidos como BLOB
 * little-endian float32 (768 dims ≈ 3KB/vector). Válido hasta ~50k chunks;
 * entonces migrar a sqlite-vec (ver research.md Decisión 4).
 */
@Service
public class EmbeddingService {

  private final EmbeddingModel embeddingModel;
  private final FragmentoRepository fragmentos;
  private final EmbeddingRepository embeddings;

  public EmbeddingService(EmbeddingModel embeddingModel, FragmentoRepository fragmentos,
      EmbeddingRepository embeddings) {
    this.embeddingModel = embeddingModel;
    this.fragmentos = fragmentos;
    this.embeddings = embeddings;
  }

  public record ScoredChunk(Fragmento fragmento, double score) {}

  public float[] embed(String text) {
    float[] v = embeddingModel.embed(text);
    double norm = 0;
    for (float f : v) norm += (double) f * f;
    norm = Math.sqrt(norm);
    if (norm == 0) return v;
    for (int i = 0; i < v.length; i++) v[i] /= norm;
    return v;
  }

  public byte[] encode(float[] v) {
    ByteBuffer buf = ByteBuffer.allocate(v.length * 4).order(ByteOrder.LITTLE_ENDIAN);
    for (float f : v) buf.putFloat(f);
    return buf.array();
  }

  public float[] decode(byte[] bytes) {
    ByteBuffer buf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] v = new float[bytes.length / 4];
    for (int i = 0; i < v.length; i++) v[i] = buf.getFloat();
    return v;
  }

  public static double dot(float[] a, float[] b) {
    double s = 0;
    for (int i = 0; i < a.length; i++) s += (double) a[i] * b[i];
    return s;
  }

  public List<ScoredChunk> search(String query, int topK) {
    float[] q = embed(query);
    List<ChunkEmbedding> all = embeddings.findAll();
    List<ScoredChunk> scored = new ArrayList<>(all.size());
    for (ChunkEmbedding e : all) {
      float[] v = decode(e.getEmbedding());
      if (v.length != q.length) continue;
      double s = dot(q, v);
      scored.add(new ScoredChunk(fragmentos.findById(e.getChunkId()).orElse(null), s));
    }
    scored.removeIf(s -> s.fragmento() == null);
    scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());
    return scored.subList(0, Math.min(topK, scored.size()));
  }
}
