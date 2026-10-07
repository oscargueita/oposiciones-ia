package com.examprep.syllabus;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SearchControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TopicRepository topics;
  @Autowired FragmentRepository fragments;
  @MockBean EmbeddingService embeddingService;

  @Test
  void searchReturnsTopicAndPageCitation() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    Topic t = topics.save(new Topic("topic01", "topic01.pdf", ContentHash.of("a".repeat(64)), 3));
    Fragment f = fragments.save(new Fragment(t.getId(), 0, 2, "el plazo es de quince dias"));
    when(embeddingService.search(anyString(), anyInt()))
        .thenReturn(List.of(new EmbeddingService.ScoredChunk(f, 0.95)));
    mvc.perform(get("/api/v1/buscar").param("q", "plazo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].topicId").value(t.getId()))
        .andExpect(jsonPath("$[0].page").value(2));
  }
}
