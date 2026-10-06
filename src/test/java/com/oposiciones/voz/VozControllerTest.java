package com.oposiciones.voz;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemarioService;
import com.oposiciones.temario.TemarioServiceTest;
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
class VozControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TemarioService temario;
  @Autowired NarracionService narracion;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  private long temaId;
  private long fragmentoId;

  @BeforeEach
  void setup() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.sintetizar(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(10.0), 10.0));
    TestAudio.limpiar(Path.of("target/test-audio"));
    MockMultipartFile file = new MockMultipartFile("files", "tema01.pdf",
        "application/pdf", TemarioServiceTest.temaValido());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    temaId = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    fragmentoId = narracion.playlist(temaId).get(0).fragmentoId();
  }

  @Test
  void playlistOrdenadaConAudio() throws Exception {
    mvc.perform(get("/api/v1/temas/" + temaId + "/narracion"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").isNumber())
        .andExpect(jsonPath("$[0].audioUrl").exists());
  }

  @Test
  void audioDevuelveWav() throws Exception {
    mvc.perform(get("/api/v1/fragmentos/" + fragmentoId + "/audio"))
        .andExpect(status().isOk())
        .andExpect(header().string("Content-Type", "audio/wav"));
  }

  @Test
  void progresoGuardadoRecuperadoYBorrado() throws Exception {
    mvc.perform(put("/api/v1/temas/" + temaId + "/progreso")
            .contentType("application/json")
            .content("{\"fragmentoId\":" + fragmentoId + ",\"offsetSeg\":4.25}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.offsetSeg").value(4.25));
    mvc.perform(get("/api/v1/temas/" + temaId + "/progreso"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fragmentoId").value(fragmentoId));
    mvc.perform(delete("/api/v1/temas/" + temaId + "/progreso"))
        .andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/temas/" + temaId + "/progreso"))
        .andExpect(status().isNotFound());
  }

  @Test
  void progresoInvalidoDevuelve422() throws Exception {
    mvc.perform(put("/api/v1/temas/" + temaId + "/progreso")
            .contentType("application/json")
            .content("{\"fragmentoId\":" + fragmentoId + ",\"offsetSeg\":999.0}"))
        .andExpect(status().is(422));
  }
}
