package com.examprep.voice;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.SyllabusService;
import com.examprep.syllabus.SyllabusServiceTest;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
class VoiceControllerTest {

  @Autowired MockMvc mvc;
  @Autowired SyllabusService temario;
  @Autowired NarrationService narration;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  private long topicId;
  private long fragmentId;

  @BeforeEach
  void setup() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.synthesize(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(10.0), 10.0, "m4a"));
    TestAudio.limpiar(Path.of("target/test-audio"));
    MockMultipartFile file = new MockMultipartFile("files", "topic01.pdf",
        "application/pdf", SyllabusServiceTest.validTopic());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    topicId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    fragmentId = narration.playlist(topicId).get(0).fragmentId();
  }

  @Test
  void orderedPlaylistWithAudio() throws Exception {
    mvc.perform(get("/api/v1/temas/" + topicId + "/narracion"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").isNumber())
        .andExpect(jsonPath("$[0].audioUrl").exists());
  }

  @Test
  void audioReturnsWav() throws Exception {
    mvc.perform(get("/api/v1/fragmentos/" + fragmentId + "/audio"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "audio/mp4"));
  }

  @Test
  void progressSavedRecoveredAndDeleted() throws Exception {
    mvc.perform(put("/api/v1/temas/" + topicId + "/progreso")
            .contentType("application/json")
            .content("{\"fragmentId\":" + fragmentId + ",\"offsetSec\":4.25}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offsetSec").value(4.25));
    mvc.perform(get("/api/v1/temas/" + topicId + "/progreso"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fragmentId").value(fragmentId));
    mvc.perform(delete("/api/v1/temas/" + topicId + "/progreso"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/temas/" + topicId + "/progreso"))
        .andExpect(status().isNotFound());
  }

  @Test
  void progresoInvalidoDevuelve422() throws Exception {
    mvc.perform(put("/api/v1/temas/" + topicId + "/progreso")
            .contentType("application/json")
            .content("{\"fragmentId\":" + fragmentId + ",\"offsetSec\":999.0}"))
        .andExpect(status().is(422));
  }
}
