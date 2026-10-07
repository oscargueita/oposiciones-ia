package com.examprep.exam;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
class ExamControllerTest {

  @Autowired MockMvc mvc;
  @Autowired SyllabusService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  private long topicId;

  @BeforeEach
  void setup() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(TestGeneratorTest.JSON_OK)))));
    MockMultipartFile file = new MockMultipartFile("files", "t.pdf",
        "application/pdf", SyllabusServiceTest.validTopic());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    topicId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
  }

  @Test
  void generateAnswerFinishAndHistory() throws Exception {
    String created = mvc.perform(post("/api/v1/temas/" + topicId + "/tests")
            .param("n", "2").param("difficulty", "HARD"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.questions.length()").value(2))
        .andExpect(jsonPath("$.questions[0].options.length()").value(4))
        .andReturn().getResponse().getContentAsString();
    var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(created);
    long testId = json.get("id").asLong();
    long pregId = json.get("questions").get(0).get("id").asLong();
    mvc.perform(post("/api/v1/tests/" + testId + "/responder")
            .contentType("application/json")
            .content("{\"questionId\":" + pregId + ",\"option\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.correct").value(true));
    mvc.perform(post("/api/v1/tests/" + testId + "/finalizar"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(5.0));
    mvc.perform(get("/api/v1/temas/" + topicId + "/tests"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].score").value(5.0));
  }

  @Test
  void dificultadInvalidaDevuelve422() throws Exception {
    mvc.perform(post("/api/v1/temas/" + topicId + "/tests").param("difficulty", "EXTREMA"))
        .andExpect(status().is(422));
  }

  @Test
  void mixedFromAllAndGlobalHistory() throws Exception {
    mvc.perform(post("/api/v1/tests").param("n", "2").param("difficulty", "EASY"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.topicId").doesNotExist())
        .andExpect(jsonPath("$.alcance").value(org.hamcrest.Matchers.startsWith("Todos")))
        .andExpect(jsonPath("$.questions.length()").value(2));
    mvc.perform(get("/api/v1/tests"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }
}
