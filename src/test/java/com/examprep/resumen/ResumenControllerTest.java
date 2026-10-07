package com.examprep.resumen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.SyllabusService;
import com.examprep.syllabus.SyllabusServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ResumenControllerTest {

  @Autowired MockMvc mvc;
  @Autowired SyllabusService syllabus;
  @Autowired com.examprep.syllabus.TopicRepository topics;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  private long topicId;

  @BeforeEach
  void setup() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(CheatsheetServiceTest.MD_OK)))));
    MockMultipartFile file = new MockMultipartFile("files", "topic01.pdf",
        "application/pdf", SyllabusServiceTest.validTopic());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    topicId = new com.fasterxml.jackson.databind.ObjectMapper().readTree(body).get(0).get("id").asLong();
  }

  @Test
  void chuletaEndpointServesMarkdown() throws Exception {
    mvc.perform(get("/api/v1/temas/" + topicId + "/chuleta"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "text/markdown;charset=UTF-8"));
  }

  @Test
  void mapaEndpointServesText() throws Exception {
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(MindmapServiceTest.MM_OK)))));
    mvc.perform(get("/api/v1/temas/" + topicId + "/mapa"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "text/plain;charset=UTF-8"));
  }

  @Test
  void missingAndInvalidTopics() throws Exception {
    mvc.perform(get("/api/v1/temas/999999/chuleta")).andExpect(status().isNotFound());
    mvc.perform(get("/api/v1/temas/999999/mapa")).andExpect(status().isNotFound());
  }

  @Test
  void emptyReadyTopicReturns422() throws Exception {
    com.examprep.syllabus.Topic empty = topics.save(new com.examprep.syllabus.Topic(
        "empty", "empty.pdf",
        com.examprep.syllabus.ContentHash.of("2".repeat(64)), 1));
    empty.markReady();
    mvc.perform(get("/api/v1/temas/" + empty.getId() + "/chuleta")).andExpect(status().is(422));
    mvc.perform(get("/api/v1/temas/" + empty.getId() + "/mapa")).andExpect(status().is(422));
  }
}
