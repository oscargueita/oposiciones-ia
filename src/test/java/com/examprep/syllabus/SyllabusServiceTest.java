package com.examprep.syllabus;

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
public class SyllabusServiceTest {

  @Autowired SyllabusService service;
  @Autowired TopicRepository topics;
  @MockBean EmbeddingService embeddingService;

  private void stubEmbed() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1, 0, 0, 0, 0, 0, 0, 0});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  public static byte[] validTopic() {
    String relleno = "Relleno. ".repeat(300);
    return TestPdf.ofPages(
        "TEMA 1. La Constitucion\n\n" + relleno,
        "Continuacion del TEMA 1\nEl plazo es de quince dias habiles para recurrir.",
        "Fin del TEMA 1\nDisposicion final unica.");
  }

  @Test
  void validIngestCreatesReadyTopicWithFragments() {
    stubEmbed();
    Topic t = service.ingest("topic01.pdf", validTopic());
    assertThat(t.getId()).isNotNull();
    assertThat(t.getTitle()).isEqualTo("topic01");
    assertThat(t.getStatus()).isEqualTo(Topic.Status.READY);
    assertThat(t.getPageCount()).isEqualTo(3);
    assertThat(service.fragmentsOf(t.getId())).isNotEmpty();
  }

  @Test
  void corruptPdfRejectedWithoutPartialTopic() {
    stubEmbed();
    assertThatThrownBy(() -> service.ingest("broken.pdf", new byte[]{1, 2, 3, 4, 5}))
        .isInstanceOf(SyllabusException.class);
    assertThat(topics.findAll()).isEmpty();
  }

  @Test
  void textlessPdfRejected() {
    stubEmbed();
    assertThatThrownBy(() -> service.ingest("scanned.pdf", TestPdf.ofPages(" ")))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("Sin text");
  }

  @Test
  void multiTopicPdfRejected() {
    stubEmbed();
    byte[] pdf = TestPdf.ofPages("TEMA 1. Primer topic\nContenido uno.", "TEMA 2. Segundo topic\nContenido dos.");
    assertThatThrownBy(() -> service.ingest("multitopic.pdf", pdf))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("1 PDF = 1 topic");
  }

  @Test
  void sameContentWithOtherNameIsDuplicate() {
    stubEmbed();
    service.ingest("topic01.pdf", validTopic());
    assertThatThrownBy(() -> service.ingest("renamed.pdf", validTopic()))
        .isInstanceOf(SyllabusException.class)
        .hasMessageContaining("Ya cargado");
  }

  @Test
  void replaceKeepsTitleAndChangesContent() {
    stubEmbed();
    Topic t = service.ingest("topic01.pdf", validTopic());
    service.rename(t.getId(), "Mi title custom");
    byte[] v2 = TestPdf.ofPages("TEMA 1. Version nueva\nTexto actualizado del topic.");
    Topic r = service.replace(t.getId(), "topic01-v2.pdf", v2);
    assertThat(r.getTitle()).isEqualTo("Mi title custom");
    assertThat(r.getStatus()).isEqualTo(Topic.Status.READY);
    assertThat(service.fragmentsOf(t.getId()).stream().map(Fragment::getText))
        .anyMatch(s -> s.contains("actualizado"));
  }

  @Test
  void replaceWithIdenticalPdfRejected() {
    stubEmbed();
    Topic t = service.ingest("topic01.pdf", validTopic());
    assertThatThrownBy(() -> service.replace(t.getId(), "topic01.pdf", validTopic()))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void deleteRemovesTopicAndFragments() {
    stubEmbed();
    Topic t = service.ingest("topic01.pdf", validTopic());
    service.delete(t.getId());
    assertThat(topics.findById(t.getId())).isEmpty();
    assertThat(service.list()).isEmpty();
  }

  @Test
  void blankRenameRejected() {
    stubEmbed();
    Topic t = service.ingest("topic01.pdf", validTopic());
    assertThatThrownBy(() -> service.rename(t.getId(), "  "))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void listShowsFragmentCount() {
    stubEmbed();
    service.ingest("topic01.pdf", validTopic());
    List<SyllabusService.TopicView> lista = service.list();
    assertThat(lista).hasSize(1);
    assertThat(lista.get(0).fragmentCount()).isPositive();
  }
}
