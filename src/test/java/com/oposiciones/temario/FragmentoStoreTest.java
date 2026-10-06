package com.oposiciones.temario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FragmentoStoreTest {

  @Autowired TemarioService service;
  @MockBean EmbeddingService embeddingService;

  @Test
  void fragmentosSonLiteralesConCitaDePagina() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{0, 1, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    Tema t = service.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    var frags = service.fragmentosDe(t.getId());
    assertThat(frags.stream().map(Fragmento::getTexto))
        .anyMatch(s -> s.contains("quince dias habiles"));
    var citado = frags.stream().filter(f -> f.getTexto().contains("quince dias habiles")).findFirst();
    assertThat(citado).isPresent();
    assertThat(citado.get().getPagina()).isEqualTo(2);
    assertThat(citado.get().getTemaId()).isEqualTo(t.getId());
  }
}
