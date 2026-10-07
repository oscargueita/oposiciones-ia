package com.examprep.voice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.ContentHash;
import com.examprep.syllabus.TopicRepository;
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
class NarrationServiceTest {

  @Autowired NarrationService narration;
  @Autowired SyllabusService temario;
  @Autowired TopicRepository topics;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  @BeforeEach
  void stub() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.synthesize(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(2.0), 2.0, "m4a"));
    TestAudio.limpiar(Path.of("target/test-audio"));
  }

  @Test
  void backgroundGeneratesOrderedPlaylistWithDuration() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    // Tests invoke synchronous generation (@Async does not see the test tx);
    // async wiring is validated on real startup.
    for (var item : narration.playlist(t.getId())) {
      narration.audioOf(item.fragmentId());
    }
    var items = narration.playlist(t.getId());
    assertThat(items).isNotEmpty();
    assertThat(items).allMatch(i -> i.durationSec() != null && i.durationSec() == 2.0);
    assertThat(items.stream().map(NarrationService.PlaylistItem::sequence).toList())
        .isSorted();
    assertThat(items.get(0).audioUrl()).endsWith("/audio");
  }

  @Test
  void audioReturnsFragmentWav() {
    Topic t = temario.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    Long fid = narration.playlist(t.getId()).get(0).fragmentId();
    byte[] wav = narration.audioOf(fid).data();
    assertThat(new String(wav, 0, 4)).isEqualTo("RIFF");
  }

  @Test
  void emptyTopicCannotBeNarrated() {
    Topic t = topics.save(new Topic("vacio", "vacio.pdf", ContentHash.of("0".repeat(64)), 1));
    t.markReady();
    assertThatThrownBy(() -> narration.playlist(t.getId()))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("sin contenido");
  }

  @Test
  void processingTopicCannotBeNarrated() {
    Topic t = topics.save(
        new Topic("proc", "proc.pdf", ContentHash.of("1".repeat(64)), 2));
    assertThatThrownBy(() -> narration.playlist(t.getId()))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("no listo");
  }
}
