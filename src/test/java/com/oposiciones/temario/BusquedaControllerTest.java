package com.oposiciones.temario;

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
class BusquedaControllerTest {

  @Autowired MockMvc mvc;
  @Autowired TemaRepository temas;
  @Autowired FragmentoRepository fragmentos;
  @MockBean EmbeddingService embeddingService;

  @Test
  void buscarDevuelveCitaTemaYPagina() throws Exception {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    Tema t = temas.save(new Tema("tema01", "tema01.pdf", "abc", 3));
    Fragmento f = fragmentos.save(new Fragmento(t.getId(), 0, 2, "el plazo es de quince dias"));
    when(embeddingService.search(anyString(), anyInt()))
        .thenReturn(List.of(new EmbeddingService.ScoredChunk(f, 0.95)));
    mvc.perform(get("/api/v1/buscar").param("q", "plazo"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].temaId").value(t.getId()))
        .andExpect(jsonPath("$[0].pagina").value(2));
  }
}
