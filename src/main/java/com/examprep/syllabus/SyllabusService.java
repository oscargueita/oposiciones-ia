package com.examprep.syllabus;

import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Orchestrates ingestion: validates, extracts, chunks, embeds and persists in batches. */
@Service
public class SyllabusService {

  static final int MIN_TEXT_CHARS = 50;

  private final TopicRepository topics;
  private final FragmentRepository fragments;
  private final EmbeddingRepository embeddings;
  private final PdfTextExtractor extractor;
  private final TextChunker chunker;
  private final EmbeddingService embeddingService;
  private final ApplicationEventPublisher eventos;

  public SyllabusService(TopicRepository topics, FragmentRepository fragments,
      EmbeddingRepository embeddings, PdfTextExtractor extractor, TextChunker chunker,
      EmbeddingService embeddingService, ApplicationEventPublisher eventos) {
    this.topics = topics;
    this.fragments = fragments;
    this.embeddings = embeddings;
    this.extractor = extractor;
    this.chunker = chunker;
    this.embeddingService = embeddingService;
    this.eventos = eventos;
  }

  public record TopicView(Topic topic, long fragmentCount) {}

  @Transactional
  public Topic ingest(String filename, byte[] bytes) {
    ContentHash hash = ContentHash.ofBytes(bytes);
    if (topics.findByContentHash(hash).isPresent()) {
      throw new SyllabusException("Ya cargado (mismo contenido): " + filename, 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new SyllabusException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declared = chunker.declaredTopics(doc.fullText());
    if (declared.size() > 1) {
      throw new SyllabusException(
          "Divide el fichero, 1 PDF = 1 tema en v1 (detectados temas " + declared + "): " + filename, 422);
    }
    String title = filename.replaceFirst("\\.[^.]+$", "");
    Topic topic = new Topic(title, filename, hash, doc.pages().size());
    topics.save(topic);
    index(topic, doc);
    topic.markReady();
    eventos.publishEvent(new TopicReadyEvent(topic.getId()));
    return topic;
  }

  @Transactional
  public Topic replace(Long id, String filename, byte[] bytes) {
    Topic topic = topics.findById(id)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    ContentHash hash = ContentHash.ofBytes(bytes);
    if (topics.findByContentHash(hash).filter(t -> !t.getId().equals(id)).isPresent()) {
      throw new SyllabusException("Ese contenido ya está cargado en otro tema", 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new SyllabusException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declared = chunker.declaredTopics(doc.fullText());
    if (declared.size() > 1) {
      throw new SyllabusException(
          "Divide el fichero, 1 PDF = 1 tema en v1 (detectados temas " + declared + ")", 422);
    }
    // The custom title is preserved (clarification A): only content changes.
    topic.beginReplacement(hash, doc.pages().size());
    deleteContent(topic.getId());
    index(topic, doc);
    topic.markReady();
    eventos.publishEvent(new TopicReadyEvent(topic.getId()));
    return topic;
  }

  @Transactional
  public Topic rename(Long id, String title) {
    Topic topic = topics.findById(id)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    topic.rename(title);
    return topic;
  }

  @Transactional
  public void delete(Long id) {
    Topic topic = topics.findById(id)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    deleteContent(id);
    topics.delete(topic);
  }

  @Transactional(readOnly = true)
  public List<TopicView> list() {
    return topics.findAll().stream()
        .map(t -> new TopicView(t, fragments.countByTopicId(t.getId())))
        .toList();
  }

  @Transactional(readOnly = true)
  public List<Fragment> fragmentsOf(Long id) {
    requireExists(id);
    return fragments.findByTopicIdOrderBySequenceAsc(id);
  }

  private void requireExists(Long id) {
    if (!topics.existsById(id)) throw new SyllabusException("Tema no existe: " + id, 404);
  }

  private void index(Topic topic, PdfTextExtractor.PdfDocument doc) {
    var chunks = chunker.chunk(doc.pages());
    for (var c : chunks) {
      Fragment f = fragments.save(new Fragment(topic.getId(), c.sequence(), c.page(), c.text()));
      float[] v = embeddingService.embed(c.text());
      embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(v)));
    }
  }

  private void deleteContent(Long topicId) {
    var frags = fragments.findByTopicIdOrderBySequenceAsc(topicId);
    embeddings.deleteByChunkIdIn(frags.stream().map(Fragment::getId).toList());
    fragments.deleteByTopicId(topicId);
    eventos.publishEvent(new TopicContentDeletedEvent(topicId));
  }
}
