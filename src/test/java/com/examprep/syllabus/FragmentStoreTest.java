package com.examprep.syllabus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FragmentStoreTest {

  @Autowired SyllabusService service;
  @MockBean EmbeddingService embeddingService;

  @Test
  void fragmentsAreLiteralWithPageCitation() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{0, 1, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    Topic t = service.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    var frags = service.fragmentsOf(t.getId());
    assertThat(frags.stream().map(Fragment::getText))
        .anyMatch(s -> s.contains("quince dias habiles"));
    var citado = frags.stream().filter(f -> f.getText().contains("quince dias habiles")).findFirst();
    assertThat(citado).isPresent();
    assertThat(citado.get().getPage()).isEqualTo(2);
    assertThat(citado.get().getTopicId()).isEqualTo(t.getId());
  }
}
