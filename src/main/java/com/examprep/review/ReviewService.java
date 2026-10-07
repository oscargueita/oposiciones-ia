package com.examprep.review;

import com.examprep.syllabus.EmbeddingRepository;
import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Fragment;
import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * In-memory hybrid ranking: 0.5·cosine + 0.5·literal bonus.
 * Cutoff 0.35: below it, no results (zero hallucinations).
 */
@Service
public class ReviewService {

  static final double UMBRAL = 0.35;
  static final int TOPK_DEFECTO = 5;

  private final FragmentRepository fragments;
  private final TopicRepository topics;
  private final EmbeddingRepository embeddings;
  private final EmbeddingService embeddingService;

  public ReviewService(FragmentRepository fragments, TopicRepository topics,
      EmbeddingRepository embeddings, EmbeddingService embeddingService) {
    this.fragments = fragments;
    this.topics = topics;
    this.embeddings = embeddings;
    this.embeddingService = embeddingService;
  }

  public record Candidate(Long fragmentId, Long topicId, int page, String text,
      double score, String audioUrl) {}

  @Transactional(readOnly = true)
  public List<Candidate> review(String q, Long topicId, int topK) {
    String consulta = q == null ? "" : q.strip();
    if (consulta.length() < 2 || consulta.length() > 500) {
      throw new SyllabusException("Indica una palabra o frase de 2 a 500 caracteres", 422);
    }
    if (topicId != null && !topics.existsById(topicId)) {
      throw new SyllabusException("Tema no existe: " + topicId, 404);
    }
    List<Fragment> base = topicId == null
        ? fragments.findAll()
        : fragments.findByTopicIdOrderBySequenceAsc(topicId);
    float[] vq = embeddingService.embed(consulta);
    String nq = TextNormalizer.normalize(consulta);
    List<String> palabras = List.of(nq.split("\\s+"));
    List<Candidate> out = new ArrayList<>();
    var embMap = embeddings.findAll().stream()
        .collect(java.util.stream.Collectors.toMap(
            com.examprep.syllabus.ChunkEmbedding::getChunkId, e -> e,
            (a, b) -> a));
    for (Fragment f : base) {
      var emb = embMap.get(f.getId());
      if (emb == null) continue;
      float[] v = embeddingService.decode(emb.getEmbedding());
      if (v.length != vq.length) continue;
      double coseno = EmbeddingService.dot(vq, v);
      String nt = TextNormalizer.normalize(f.getText());
      double bonus = nt.contains(nq) ? 1.0
          : palabras.stream().allMatch(nt::contains) ? 0.3 : 0.0;
      double score = 0.5 * coseno + 0.5 * bonus;
      if (score >= UMBRAL) {
        out.add(new Candidate(f.getId(), f.getTopicId(), f.getPage(), f.getText(), score,
            "/api/v1/fragments/" + f.getId() + "/audio"));
      }
    }
    out.sort(Comparator.comparingDouble(Candidate::score).reversed());
    return out.subList(0, Math.min(Math.max(topK, 1), out.size()));
  }
}
