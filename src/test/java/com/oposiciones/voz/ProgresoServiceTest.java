package com.oposiciones.voz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Tema;
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
class ProgresoServiceTest {

  @Autowired NarracionService narracion;
  @Autowired TemarioService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean TtsService tts;

  @BeforeEach
  void stub() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(tts.sintetizar(anyString()))
        .thenReturn(new TtsService.Audio(TestAudio.wav(10.0), 10.0, "m4a"));
    TestAudio.limpiar(Path.of("target/test-audio"));
  }

  @Test
  void guardarYRecuperarSegundoExacto() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    narracion.guardarProgreso(t.getId(), fid, 7.5);
    var p = narracion.progresoDe(t.getId());
    assertThat(p).isPresent();
    assertThat(p.get().getFragmentoId()).isEqualTo(fid);
    assertThat(p.get().getOffsetSeg()).isEqualTo(7.5);
  }

  @Test
  void offsetMayorQueDuracionSeRechaza() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    assertThatThrownBy(() -> narracion.guardarProgreso(t.getId(), fid, 99.0))
        .isInstanceOf(TemarioException.class);
  }

  @Test
  void fragmentoDeOtroTemaSeRechaza() {
    Tema a = temario.ingestar("a.pdf", TemarioServiceTest.temaValido());
    Tema b = temario.ingestar("b.pdf",
        com.oposiciones.temario.TestPdf.ofPages("TEMA 9. Otro\nContenido distinto del otro tema para narracion."));
    Long fidB = narracion.playlist(b.getId()).get(0).fragmentoId();
    assertThatThrownBy(() -> narracion.guardarProgreso(a.getId(), fidB, 1.0))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("no pertenece");
  }

  @Test
  void terminarBorraProgreso() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    narracion.guardarProgreso(t.getId(), fid, 3.0);
    narracion.terminarProgreso(t.getId());
    assertThat(narracion.progresoDe(t.getId())).isEmpty();
  }

  @Test
  void reemplazarInvalidaProgreso() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    narracion.guardarProgreso(t.getId(), fid, 3.0);
    temario.reemplazar(t.getId(), "v2.pdf",
        com.oposiciones.temario.TestPdf.ofPages("TEMA 1. Nuevo\nContenido nuevo del tema con texto suficiente para la narracion de prueba."));
    assertThat(narracion.progresoDe(t.getId())).isEmpty();
  }

  @Test
  void borrarTemaEliminaProgreso() {
    Tema t = temario.ingestar("tema01.pdf", TemarioServiceTest.temaValido());
    Long fid = narracion.playlist(t.getId()).get(0).fragmentoId();
    narracion.guardarProgreso(t.getId(), fid, 3.0);
    temario.borrar(t.getId());
    // el tema ya no existe: progresoDe lanza 404
    assertThatThrownBy(() -> narracion.progresoDe(t.getId()))
        .isInstanceOf(TemarioException.class);
  }
}
