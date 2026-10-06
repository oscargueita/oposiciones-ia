package com.oposiciones.repaso;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.oposiciones.temario.ChunkEmbedding;
import com.oposiciones.temario.EmbeddingRepository;
import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Fragmento;
import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemaRepository;
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
class RepasoControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TemaRepository temas;
  @Autowired FragmentoRepository fragmentos;
  @Autowired EmbeddingRepository embeddings;
  @MockBean EmbeddingService embeddingService;

  private long temaId;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(embeddingService.decode(org.mockito.ArgumentMatchers.any(byte[].class)))
        .thenCallRealMethod();
    Tema t = temas.save(new Tema("t", "t.pdf", "rc" + System.nanoTime(), 1));
    temaId = t.getId();
    Fragmento f = fragmentos.save(new Fragmento(t.getId(), 0, 3, "El recurso de alzada se interpone en un mes."));
    embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(new float[]{0.6f, 0.8f, 0})));
  }

  @Test
  void repasoDevuelveCandidatoConCitaYAudio() throws Exception {
    mvc.perform(get("/api/v1/repasar").param("q", "recurso de alzada"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].temaId").value(temaId))
        .andExpect(jsonPath("$[0].pagina").value(3))
        .andExpect(jsonPath("$[0].audioUrl").exists());
  }

  @Test
  void repasoAcotadoYErrores() throws Exception {
    mvc.perform(get("/api/v1/repasar").param("q", "alzada").param("temaId", String.valueOf(temaId)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
    mvc.perform(get("/api/v1/repasar").param("q", "a")).andExpect(status().is(422));
    mvc.perform(get("/api/v1/repasar").param("q", "alzada").param("temaId", "999999"))
        .andExpect(status().isNotFound());
  }
}
