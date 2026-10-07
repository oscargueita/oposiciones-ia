package com.examprep.review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.examprep.syllabus.ChunkEmbedding;
import com.examprep.syllabus.EmbeddingRepository;
import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Fragment;
import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReviewServiceTest {

  @Autowired ReviewService repaso;
  @Autowired TopicRepository topics;
  @Autowired FragmentRepository fragments;
  @Autowired EmbeddingRepository embeddings;
  @MockBean EmbeddingService embeddingService;

  private Topic topic;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0});
    when(embeddingService.embed("xyzqwerty")).thenReturn(new float[]{0, 0, -1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(embeddingService.decode(org.mockito.ArgumentMatchers.any(byte[].class)))
        .thenCallRealMethod();
    topic = topics.save(new Topic("t", "t.pdf", "sh" + System.nanoTime(), 1));
    save("El recurso de alzada se interpone en un mes.", new float[]{0.6f, 0.8f, 0});
    save("La organización administrativa y sus principios rectores.", new float[]{1, 0, 0});
    save("Sobre contratación del sector público y licitaciones.", new float[]{0, 0, 1});
  }

  private void save(String text, float[] v) {
    Fragment f = fragments.save(new Fragment(topic.getId(), 0, 1, text));
    embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(v)));
  }

  @Test
  void exactLiteralWinsAndNoResultsIsEmpty() {
    var res = repaso.review("recurso de alzada", null, 5);
    assertThat(res).hasSize(2);
    assertThat(res.get(0).text()).contains("recurso de alzada");
    assertThat(res.get(0).audioUrl()).endsWith("/audio");
    assertThat(repaso.review("xyzqwerty", null, 5)).isEmpty();
  }

  @Test
  void caseAndAccentInsensitive() {
    var res = repaso.review("RECURSO DE ALZADA", null, 5);
    assertThat(res).isNotEmpty();
    assertThat(TextNormalizer.normalize("Recurso de Alzada")).isEqualTo("recurso de alzada");
  }

  @Test
  void shortQueryRejected() {
    assertThatThrownBy(() -> repaso.review("a", null, 5))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void scopedFiltersByTopic() {
    Topic otro = topics.save(new Topic("o", "o.pdf", "ot" + System.nanoTime(), 1));
    var res = repaso.review("recurso", otro.getId(), 5);
    assertThat(res).isEmpty();
    assertThatThrownBy(() -> repaso.review("recurso", 999999L, 5))
        .isInstanceOf(SyllabusException.class);
  }
}
