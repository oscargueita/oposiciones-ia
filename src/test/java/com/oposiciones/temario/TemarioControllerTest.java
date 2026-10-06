package com.oposiciones.temario;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class TemarioControllerTest {

  @Autowired MockMvc mvc;
  @MockBean EmbeddingService embeddingService;

  private void stubEmbed() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  @Test
  void subirYListar() throws Exception {
    stubEmbed();
    MockMultipartFile file = new MockMultipartFile("files", "tema01.pdf",
        "application/pdf", TemarioServiceTest.temaValido());
    mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$[0].titulo").value("tema01"))
        .andExpect(jsonPath("$[0].estado").value("LISTO"));
    mvc.perform(multipart("/api/v1/temas").file(file)).andExpect(status().is4xxClientError());
    mvc.perform(get("/api/v1/temas"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  @Test
  void pdfCorruptoDevuelve422() throws Exception {
    stubEmbed();
    MockMultipartFile file = new MockMultipartFile("files", "roto.pdf",
        "application/pdf", new byte[]{9, 9, 9});
    mvc.perform(multipart("/api/v1/temas").file(file)).andExpect(status().is(422));
  }

  @Test
  void renombrarReemplazarYBorrar() throws Exception {
    stubEmbed();
    MockMultipartFile file = new MockMultipartFile("files", "tema01.pdf",
        "application/pdf", TemarioServiceTest.temaValido());
    String body = mvc.perform(multipart("/api/v1/temas").file(file))
        .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
    long id = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    mvc.perform(patch("/api/v1/temas/" + id + "/titulo")
            .contentType("application/json").content("{\"titulo\":\"Custom\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Custom"));
    MockMultipartFile v2 = new MockMultipartFile("file", "v2.pdf",
        "application/pdf", TestPdf.ofPages("TEMA 1. Nuevo\nContenido nuevo del tema con texto suficiente para superar el minimo exigido de caracteres."));
    mvc.perform(multipart("/api/v1/temas/" + id + "/pdf").file(v2).with(r -> {
      r.setMethod("PUT");
      return r;
    })).andExpect(status().isOk())
        .andExpect(jsonPath("$.titulo").value("Custom"));
    mvc.perform(delete("/api/v1/temas/" + id)).andExpect(status().isNoContent());
    mvc.perform(get("/api/v1/temas")).andExpect(jsonPath("$.length()").value(0));
  }
}
