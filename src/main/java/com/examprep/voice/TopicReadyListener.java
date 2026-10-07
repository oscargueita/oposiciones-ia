package com.examprep.voice;

import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.TopicContentDeletedEvent;
import com.examprep.syllabus.TopicReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Generates WAVs in the background after ingestion; cleans up on delete/replace. */
@Component
public class TopicReadyListener {

  private final FragmentRepository fragments;
  private final NarrationService narration;
  private final ListeningProgressRepository progresos;
  private final AudioStore store;

  public TopicReadyListener(FragmentRepository fragments, NarrationService narration,
      ListeningProgressRepository progresos, AudioStore store) {
    this.fragments = fragments;
    this.narration = narration;
    this.progresos = progresos;
    this.store = store;
  }

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void generateAudios(TopicReadyEvent event) {
    for (var f : fragments.findByTopicIdOrderBySequenceAsc(event.topicId())) {
      narration.ensureAudio(f);
    }
  }

  @EventListener
  public void cleanUp(TopicContentDeletedEvent event) {
    progresos.deleteById(event.topicId());
    store.deleteTopic(event.topicId());
  }
}
