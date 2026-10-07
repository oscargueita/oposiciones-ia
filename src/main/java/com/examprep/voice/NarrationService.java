package com.examprep.voice;

import com.examprep.syllabus.Fragment;
import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Playlist by fragments, lazy generation and listening progress. */
@Service
public class NarrationService {

  private final TopicRepository topics;
  private final FragmentRepository fragments;
  private final FragmentAudioRepository audios;
  private final ListeningProgressRepository progresos;
  private final TtsService tts;
  private final AudioStore store;

  public NarrationService(TopicRepository topics, FragmentRepository fragments,
      FragmentAudioRepository audios, ListeningProgressRepository progresos, TtsService tts,
      AudioStore store) {
    this.topics = topics;
    this.fragments = fragments;
    this.audios = audios;
    this.progresos = progresos;
    this.tts = tts;
    this.store = store;
  }

  public record PlaylistItem(Long fragmentId, int sequence, int page, Double durationSec,
      String audioUrl) {}

  @Transactional(readOnly = true)
  public List<PlaylistItem> playlist(Long topicId) {
    Topic topic = requireReadyWithContent(topicId);
    return fragments.findByTopicIdOrderBySequenceAsc(topic.getId()).stream()
        .map(f -> new PlaylistItem(f.getId(), f.getSequence(), f.getPage(),
            audios.findById(f.getId()).map(FragmentAudio::getDurationSec).orElse(null),
            "/api/v1/fragmentos/" + f.getId() + "/audio"))
        .toList();
  }

  public record DeliveryAudio(byte[] data, String format) {}

  @Transactional
  public DeliveryAudio audioOf(Long fragmentId) {
    Fragment f = fragments.findById(fragmentId)
        .orElseThrow(() -> new SyllabusException("Fragmento no existe: " + fragmentId, 404));
    FragmentAudio row = ensureAudio(f);
    return new DeliveryAudio(store.read(f.getTopicId(), f.getId(), row.getFormat()), row.getFormat());
  }

  @Transactional
  public FragmentAudio ensureAudio(Fragment f) {
    var existente = audios.findById(f.getId());
    if (existente.isPresent() && store.exists(f.getTopicId(), f.getId(), existente.get().getFormat())) {
      return existente.get();
    }
    TtsService.Audio audio = tts.synthesize(f.getText());
    store.save(f.getTopicId(), f.getId(), audio.extension(), audio.data());
    existente.ifPresent(audios::delete);
    return guardarConReintento(f.getId(), audio);
  }

  /** Retries the INSERT on SQLITE_BUSY from concurrent writers (WAL + wait). */
  private FragmentAudio guardarConReintento(Long fragmentId, TtsService.Audio audio) {
    for (int i = 1; ; i++) {
      try {
        return audios.save(new FragmentAudio(fragmentId, audio.durationSec(), audio.extension()));
      } catch (org.springframework.dao.DataAccessException e) {
        if (i >= 5) throw e;
        try {
          Thread.sleep(200L * i);
        } catch (InterruptedException ie) {
          Thread.currentThread().interrupt();
          throw e;
        }
      }
    }
  }

  @Transactional
  public ListeningProgress saveProgress(Long topicId, Long fragmentId, double offsetSec) {
    requireExists(topicId);
    Fragment f = fragments.findById(fragmentId)
        .orElseThrow(() -> new SyllabusException("Fragmento no existe: " + fragmentId, 404));
    if (!f.belongsTo(topicId)) {
      throw new SyllabusException("El fragmento no pertenece al tema", 422);
    }
    ensureAudio(f);
    double duration = audios.findById(f.getId()).orElseThrow().getDurationSec();
    ListeningProgress p = progresos.findById(topicId)
        .orElseGet(() -> new ListeningProgress(topicId, fragmentId, 0));
    p.moveTo(fragmentId, offsetSec, duration);
    return progresos.save(p);
  }

  @Transactional(readOnly = true)
  public Optional<ListeningProgress> progressOf(Long topicId) {
    requireExists(topicId);
    return progresos.findById(topicId);
  }

  @Transactional
  public void clearProgress(Long topicId) {
    requireExists(topicId);
    progresos.deleteById(topicId);
  }

  private void requireExists(Long topicId) {
    if (!topics.existsById(topicId)) throw new SyllabusException("Tema no existe: " + topicId, 404);
  }

  private Topic requireReadyWithContent(Long topicId) {
    Topic topic = topics.findById(topicId)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + topicId, 404));
    if (topic.getStatus() != Topic.Status.READY) {
      throw new SyllabusException("Tema no listo para narrar (estado " + topic.getStatus() + ")", 422);
    }
    if (fragments.countByTopicId(topicId) == 0) {
      throw new SyllabusException("Tema sin contenido narrable", 422);
    }
    return topic;
  }
}
