package com.oposiciones.temario;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class TemarioServiceTest {

  @Autowired TemarioService service;
  @Autowired TemaRepository temas;
  @MockBean EmbeddingService embeddingService;

  private void stubEmbed() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0, 0, 0, 0, 0, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  static byte[] temaValido() {
    String relleno = "Relleno. ".repeat(300);
    return TestPdf.ofPages(
        "TEMA 1. La Constitucion\n\n" + relleno,
        "Continuacion del TEMA 1\nEl plazo es de quince dias habiles para recurrir.",
        "Fin del TEMA 1\nDisposicion final unica.");
  }

  @Test
  void ingestaValidaCreaTemaListoConFragmentos() {
    stubEmbed();
    Tema t = service.ingestar("tema01.pdf", temaValido());
    assertThat(t.getId()).isNotNull();
    assertThat(t.getTitulo()).isEqualTo("tema01");
    assertThat(t.getEstado()).isEqualTo(Tema.Estado.LISTO);
    assertThat(t.getNumPaginas()).isEqualTo(3);
    assertThat(service.fragmentosDe(t.getId())).isNotEmpty();
  }

  @Test
  void pdfCorruptoSeRechazaSinTemaParcial() {
    stubEmbed();
    assertThatThrownBy(() -> service.ingestar("roto.pdf", new byte[]{1, 2, 3, 4, 5}))
        .isInstanceOf(TemarioException.class);
    assertThat(temas.findAll()).isEmpty();
  }

  @Test
  void pdfSinTextoSeRechaza() {
    stubEmbed();
    assertThatThrownBy(() -> service.ingestar("escaneado.pdf", TestPdf.ofPages(" ")))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("Sin texto");
  }

  @Test
  void pdfMultiTemaSeRechaza() {
    stubEmbed();
    byte[] pdf = TestPdf.ofPages("TEMA 1. Primer tema\nContenido uno.", "TEMA 2. Segundo tema\nContenido dos.");
    assertThatThrownBy(() -> service.ingestar("multi.pdf", pdf))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("1 PDF = 1 tema");
  }

  @Test
  void mismoContenidoConOtroNombreEsDuplicado() {
    stubEmbed();
    service.ingestar("tema01.pdf", temaValido());
    assertThatThrownBy(() -> service.ingestar("renombrado.pdf", temaValido()))
        .isInstanceOf(TemarioException.class)
        .hasMessageContaining("Ya cargado");
  }

  @Test
  void reemplazarConservaTituloYCambiaContenido() {
    stubEmbed();
    Tema t = service.ingestar("tema01.pdf", temaValido());
    service.renombrar(t.getId(), "Mi titulo custom");
    byte[] v2 = TestPdf.ofPages("TEMA 1. Version nueva\nTexto actualizado del tema.");
    Tema r = service.reemplazar(t.getId(), "tema01-v2.pdf", v2);
    assertThat(r.getTitulo()).isEqualTo("Mi titulo custom");
    assertThat(r.getEstado()).isEqualTo(Tema.Estado.LISTO);
    assertThat(service.fragmentosDe(t.getId()).stream().map(Fragmento::getTexto))
        .anyMatch(s -> s.contains("actualizado"));
  }

  @Test
  void reemplazarConPdfIdenticoSeRechaza() {
    stubEmbed();
    Tema t = service.ingestar("tema01.pdf", temaValido());
    assertThatThrownBy(() -> service.reemplazar(t.getId(), "tema01.pdf", temaValido()))
        .isInstanceOf(TemarioException.class);
  }

  @Test
  void borrarEliminaTemaYFragmentos() {
    stubEmbed();
    Tema t = service.ingestar("tema01.pdf", temaValido());
    service.borrar(t.getId());
    assertThat(temas.findById(t.getId())).isEmpty();
    assertThat(service.listar()).isEmpty();
  }

  @Test
  void renombrarVacioSeRechaza() {
    stubEmbed();
    Tema t = service.ingestar("tema01.pdf", temaValido());
    assertThatThrownBy(() -> service.renombrar(t.getId(), "  "))
        .isInstanceOf(TemarioException.class);
  }

  @Test
  void listarMuestraNumFragmentos() {
    stubEmbed();
    service.ingestar("tema01.pdf", temaValido());
    List<TemarioService.TemaVista> lista = service.listar();
    assertThat(lista).hasSize(1);
    assertThat(lista.get(0).numFragmentos()).isPositive();
  }
}
