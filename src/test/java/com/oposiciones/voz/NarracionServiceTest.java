package com.oposiciones.voz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemaRepository;
import com.oposiciones.temario.TemarioException;
import com.oposiciones.temario.TemarioService;
import com.oposiciones.temario.TemarioServiceTest;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class NarracionServiceTest {

  @Autowired NarracionService narracion;
  @Autowired TemarioService temario;
  @Autowired TemaRepository temas;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  @BeforeEach
  void stub() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.sintetizar(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(2.0), 2.0, "m4a"));
    TestAudio.limpiar(Path.of("target/test-audio"));
  }

  @Test
  void fondoGeneraPlaylistOrdenadaConDuracion() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    // En tests se invoca la generación síncrona (@Async no ve la tx del test);
    // el cableado async se valida en arranque real.
    for (var item : narracion.playlist(t.getId())) {
      narracion.audioDe(item.fragmentoId());
    }
    var items = narracion.playlist(t.getId());
    assertThat(items).isNotEmpty();
    assertThat(items).allMatch(i -> i.duracionSeg() != null && i.duracionSeg() == 2.0);
    assertThat(items.stream().map(NarracionService.ItemPlaylist::orden).toList())
        .isSorted();
    assertThat(items.get(0).audioUrl()).endsWith("/audio");
  }

  @Test
  void audioDevuelveWavDelFragmento() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    byte[] wav = narracion.audioDe(fid).datos();
    assertThat(new String(wav, 0, 4)).isEqualTo("RIFF");
  }

  @Test
  void temaVacioNoSePuedeNarrar() {
    Tema t = temas.save(new Tema("vacio", "vacio.pdf", "deadbeef", 1));
    t.setEstado(Tema.Estado.LISTO);
    assertThatThrownBy(() -> narracion.playlist(t.getId()))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("sin contenido");
  }

  @Test
  void temaEnProcesoNoSePuedeNarrar() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    t.setEstado(Tema.Estado.PROCESANDO);
    assertThatThrownBy(() -> narracion.playlist(t.getId()))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("no listo");
  }
}
