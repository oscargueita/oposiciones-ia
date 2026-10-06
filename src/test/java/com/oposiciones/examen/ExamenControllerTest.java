package com.oposiciones.examen;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.TemarioService;
import com.oposiciones.temario.TemarioServiceTest;
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
class ExamenControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TemarioService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  private long temaId;

  @BeforeEach
  void setup() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(GeneradorTestsTest.JSON_OK)))));
    MockMultipartFile file = new MockMultipartFile("files", "t.pdf",
        "application/pdf", TemarioServiceTest.temaValido());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    temaId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
  }

  @Test
  void generarResponderFinalizarEHistorial() throws Exception {
    String creado = mvc.perform(post("/api/v1/temas/" + temaId + "/tests")
            .param("n", "2").param("dificultad", "DIFICIL"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.preguntas.length()").value(2))
        .andExpect(jsonPath("$.preguntas[0].opciones.length()").value(4))
        .andReturn().getResponse().getContentAsString();
    var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(creado);
    long testId = json.get("id").asLong();
    long pregId = json.get("preguntas").get(0).get("id").asLong();
    mvc.perform(post("/api/v1/tests/" + testId + "/responder")
            .contentType("application/json")
            .content("{\"preguntaId\":" + pregId + ",\"opcion\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.acierto").value(true));
    mvc.perform(post("/api/v1/tests/" + testId + "/finalizar"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.nota").value(5.0));
    mvc.perform(get("/api/v1/temas/" + temaId + "/tests"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].nota").value(5.0));
  }

  @Test
  void dificultadInvalidaDevuelve422() throws Exception {
    mvc.perform(post("/api/v1/temas/" + temaId + "/tests").param("dificultad", "EXTREMA"))
        .andExpect(status().is(422));
  }
}
