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
    String sha = Sha256.of(bytes);
    if (topics.findByContentSha256(sha).isPresent()) {
      throw new SyllabusException("Ya cargado (mismo contenido): " + filename, 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new SyllabusException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declarados = chunker.declaredTopics(doc.fullText());
    if (declarados.size() > 1) {
      throw new SyllabusException(
          "Divide el fichero, 1 PDF = 1 topic en v1 (detectados topics " + declarados + "): " + filename, 422);
    }
    String title = filename.replaceFirst("\\.[^.]+$", "");
    Topic topic = new Topic(title, filename, sha, doc.pages().size());
    topics.save(topic);
    index(topic, doc);
    topic.setStatus(Topic.Status.READY);
    eventos.publishEvent(new TopicReadyEvent(topic.getId()));
    return topic;
  }

  @Transactional
  public Topic replace(Long id, String filename, byte[] bytes) {
    Topic topic = topics.findById(id)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    String sha = Sha256.of(bytes);
    if (sha.equals(topic.getContentSha256())) {
      throw new SyllabusException("El PDF es idéntico al actual, nada que reemplazar", 422);
    }
    if (topics.findByContentSha256(sha).filter(t -> !t.getId().equals(id)).isPresent()) {
      throw new SyllabusException("Ese contenido ya está cargado en otro tema", 422);
    }
    var doc = extractor.extract(filename, bytes);
    if (doc.fullText().length() < MIN_TEXT_CHARS) {
      throw new SyllabusException("Sin texto extraíble (¿PDF escaneado?): " + filename, 422);
    }
    var declarados = chunker.declaredTopics(doc.fullText());
    if (declarados.size() > 1) {
      throw new SyllabusException(
          "Divide el fichero, 1 PDF = 1 topic en v1 (detectados topics " + declarados + ")", 422);
    }
    // The custom title is preserved (clarification A): only content and origin change.
    topic.setContentSha256(sha);
    topic.setPageCount(doc.pages().size());
    topic.setStatus(Topic.Status.PROCESSING);
    topic.setErrorMessage(null);
    deleteContent(topic.getId());
    index(topic, doc);
    topic.setStatus(Topic.Status.READY);
    eventos.publishEvent(new TopicReadyEvent(topic.getId()));
    return topic;
  }

  @Transactional
  public Topic rename(Long id, String title) {
    Topic topic = topics.findById(id)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    if (title == null || title.isBlank()) {
      throw new SyllabusException("El título no puede estar vacío", 422);
    }
    topic.setTitle(title.strip());
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
