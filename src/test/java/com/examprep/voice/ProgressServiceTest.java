package com.examprep.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.SyllabusException;
import com.examprep.syllabus.SyllabusService;
import com.examprep.syllabus.SyllabusServiceTest;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ProgressServiceTest {

  @Autowired NarrationService narration;
  @Autowired SyllabusService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  @BeforeEach
  void stub() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.synthesize(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(10.0), 10.0, "m4a"));
    TestAudio.limpiar(Path.of("target/test-audio"));
  }

  @Test
  void saveAndRecoverExactSecond() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    narration.saveProgress(t.getId(), fid, 7.5);
    var p = narration.progressOf(t.getId());
    assertThat(p).isPresent();
    assertThat(p.get().getFragmentId()).isEqualTo(fid);
    assertThat(p.get().getOffsetSec()).isEqualTo(7.5);
  }

  @Test
  void offsetBeyondDurationRejected() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    assertThatThrownBy(() -> narration.saveProgress(t.getId(), fid, 99.0))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void fragmentFromOtherTopicRejected() {
    Topic a = temario.ingest("a.pdf", SyllabusServiceTest.validTopic());
    Topic b = temario.ingest("b.pdf",
        com.examprep.syllabus.TestPdf.ofPages("TEMA 9. Otro\nContenido distinto del otro topic para narration."));
    Long fidB = narration.playlist(b.getId()).get(0).fragmentId();
    assertThatThrownBy(() -> narration.saveProgress(a.getId(), fidB, 1.0))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("no pertenece");
  }

  @Test
  void finishDeletesProgress() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    narration.saveProgress(t.getId(), fid, 3.0);
    narration.clearProgress(t.getId());
    assertThat(narration.progressOf(t.getId())).isEmpty();
  }

  @Test
  void replaceInvalidatesProgress() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    narration.saveProgress(t.getId(), fid, 3.0);
    temario.replace(t.getId(), "v2.pdf",
        com.examprep.syllabus.TestPdf.ofPages("TEMA 1. Nuevo\nContenido nuevo del topic con text suficiente para la narration de prueba."));
    assertThat(narration.progressOf(t.getId())).isEmpty();
  }

  @Test
  void deleteTopicRemovesProgress() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    narration.saveProgress(t.getId(), fid, 3.0);
    temario.delete(t.getId());
    // the topic no longer exists: progressOf throws 404
    assertThatThrownBy(() -> narration.progressOf(t.getId()))
        .isInstanceOf(SyllabusException.class);
  }
}
