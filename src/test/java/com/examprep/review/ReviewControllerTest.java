package com.examprep.review;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.examprep.syllabus.ChunkEmbedding;
import com.examprep.syllabus.EmbeddingRepository;
import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Fragment;
import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.ContentHash;
import com.examprep.syllabus.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
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
class ReviewControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TopicRepository topics;
  @Autowired FragmentRepository fragments;
  @Autowired EmbeddingRepository embeddings;
  @MockBean EmbeddingService embeddingService;

  private long topicId;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(embeddingService.decode(org.mockito.ArgumentMatchers.any(byte[].class)))
        .thenCallRealMethod();
    Topic t = topics.save(new Topic("t", "t.pdf", ContentHash.of(String.format("%064x", System.nanoTime()+2)), 1));
    topicId = t.getId();
    Fragment f = fragments.save(new Fragment(t.getId(), 0, 3, "El recurso de alzada se interpone en un mes."));
    embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(new float[]{0.6f, 0.8f, 0})));
  }

  @Test
  void reviewReturnsCandidateWithCitationAndAudio() throws Exception {
    mvc.perform(get("/api/v1/repasar").param("q", "recurso de alzada"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].topicId").value(topicId))
        .andExpect(jsonPath("$[0].page").value(3))
        .andExpect(jsonPath("$[0].audioUrl").exists());
  }

  @Test
  void scopedReviewAndErrors() throws Exception {
    mvc.perform(get("/api/v1/repasar").param("q", "alzada").param("topicId", String.valueOf(topicId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/v1/repasar").param("q", "a")).andExpect(status().is(422));
    mvc.perform(get("/api/v1/repasar").param("q", "alzada").param("topicId", "999999"))
        .andExpect(status().isNotFound());
  }
}
