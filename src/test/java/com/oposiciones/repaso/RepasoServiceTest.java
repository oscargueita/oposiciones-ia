package com.oposiciones.repaso;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.oposiciones.temario.ChunkEmbedding;
import com.oposiciones.temario.EmbeddingRepository;
import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Fragmento;
import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemaRepository;
import com.oposiciones.temario.TemarioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class RepasoServiceTest {

  @Autowired RepasoService repaso;
  @Autowired TemaRepository temas;
  @Autowired FragmentoRepository fragmentos;
  @Autowired EmbeddingRepository embeddings;
  @MockBean EmbeddingService embeddingService;

  private Tema tema;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0});
    when(embeddingService.embed("xyzqwerty")).thenReturn(new float[]{0, 0, -1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(embeddingService.decode(org.mockito.ArgumentMatchers.any(byte[].class)))
        .thenCallRealMethod();
    tema = temas.save(new Tema("t", "t.pdf", "sh" + System.nanoTime(), 1));
    guardar("El recurso de alzada se interpone en un mes.", new float[]{0.6f, 0.8f, 0});
    guardar("La organización administrativa y sus principios rectores.", new float[]{1, 0, 0});
    guardar("Sobre contratación del sector público y licitaciones.", new float[]{0, 0, 1});
  }

  private void guardar(String texto, float[] v) {
    Fragmento f = fragmentos.save(new Fragmento(tema.getId(), 0, 1, texto));
    embeddings.save(new ChunkEmbedding(f.getId(), embeddingService.encode(v)));
  }

  @Test
  void literalExactoGanaYSinResultadosEsVacio() {
    var res = repaso.repasar("recurso de alzada", null, 5);
    assertThat(res).hasSize(2);
    assertThat(res.get(0).texto()).contains("recurso de alzada");
    assertThat(res.get(0).audioUrl()).endsWith("/audio");
    assertThat(repaso.repasar("xyzqwerty", null, 5)).isEmpty();
  }

  @Test
  void insensibleAMayusculasYTildes() {
    var res = repaso.repasar("RECURSO DE ALZADA", null, 5);
    assertThat(res).isNotEmpty();
    assertThat(Normalizador.normalizar("Recurso de Alzada")).isEqualTo("recurso de alzada");
  }

  @Test
  void consultaCortaSeRechaza() {
    assertThatThrownBy(() -> repaso.repasar("a", null, 5))
        .isInstanceOf(TemarioException.class);
  }

  @Test
  void acotadoFiltraPorTema() {
    Tema otro = temas.save(new Tema("o", "o.pdf", "ot" + System.nanoTime(), 1));
    var res = repaso.repasar("recurso", otro.getId(), 5);
    assertThat(res).isEmpty();
    assertThatThrownBy(() -> repaso.repasar("recurso", 999999L, 5))
        .isInstanceOf(TemarioException.class);
  }
}
