package com.examprep.resumen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.SyllabusException;
import com.examprep.syllabus.SyllabusService;
import com.examprep.syllabus.SyllabusServiceTest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class MindmapServiceTest {

  @Autowired MindmapService mindmaps;
  @Autowired SyllabusService syllabus;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  static final String MM_OK = """
      mindmap
        Constitución
          Derechos fundamentales
            Igualdad
            Libertad
            Justicia
          Deberes
            Tributos
            Defensa
          Garantías
            Recurso
            Defensor""";

  private void stubBase() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  private ChatResponse answer(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  @Test
  void validatorAcceptsAndRejects() {
    assertThat(MindmapValidator.valid(MM_OK)).isTrue();
    assertThat(MindmapValidator.valid("graph TD\n  A-->B")).isFalse();
    assertThat(MindmapValidator.valid("mindmap\n  Solo raíz")).isFalse();
    assertThat(MindmapValidator.valid("mindmap\n  Nodo (con paréntesis)\n  Otro")).isFalse();
  }

  @Test
  void sanitizeStripsFences() {
    assertThat(MindmapService.normalize("```mermaid\nmindmap\n  Raíz\n  Otra\n  T1\n  T2\n  T3\n  T4\n  T5\n  T6\n```")).startsWith("mindmap");
    assertThat(MindmapService.normalize("mindmap\n  Raíz")).isEqualTo("");
  }

  @Test
  void normalizeRepairsBulletsAndCapsNodes() {
    String raw = "Aquí tienes:\n```mermaid\nmindmap\n  Raíz\n  - A\n  - B\n  * C\n  +— D\n  E\n  F\n  G\n  H\n  I\n  J\n  K\n  L\n  M\n  N\n  O\n  P\n```";
    String mm = MindmapService.normalize(raw);
    assertThat(MindmapValidator.valid(mm)).isTrue();
  }

  @Test
  void generatesPersistsAndRegeneratesAfterReplace() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(MM_OK));
    Topic t = syllabus.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    String first = mindmaps.mindmap(t.getId());
    assertThat(first).startsWith("mindmap");
    assertThat(mindmaps.mindmap(t.getId())).isEqualTo(first);
    syllabus.replace(t.getId(), "topic01-v2.pdf",
        com.examprep.syllabus.TestPdf.ofPages("TEMA 1. Nuevo\nContenido nuevo del tema con texto suficiente para la narración de prueba de mapas."));
    // invalidated: regenerates on next call instead of serving stale content
    assertThat(mindmaps.mindmap(t.getId())).startsWith("mindmap");
  }
}
