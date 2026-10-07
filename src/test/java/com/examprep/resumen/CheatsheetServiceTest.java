package com.examprep.resumen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
class CheatsheetServiceTest {

  @Autowired CheatsheetService cheatsheets;
  @Autowired SyllabusService syllabus;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  static final String MD_OK = """
      ## Plazos
      - El plazo general es de un mes (tema, pág. 2).
      - El recurso se interpone en diez días (tema, pág. 3).
      - La resolución llega en tres meses (tema, pág. 4).
      - El silencio es desestimatorio (tema, pág. 5).
      - Cabe revisión extraordinaria (tema, pág. 6).
      - La audiencia es trámite esencial (tema, pág. 7).""";

  private void stubBase() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  private ChatResponse answer(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  @Test
  void generatesPersistsAndReuses() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(MD_OK));
    Topic t = syllabus.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    String first = cheatsheets.cheatsheet(t.getId());
    assertThat(first).contains("## Plazos");
    assertThat(first).contains("pág");
    String second = cheatsheets.cheatsheet(t.getId());
    assertThat(second).isEqualTo(first);
    verify(chatModel, times(1)).call(any(Prompt.class));
  }

  @Test
  void invalidOutputRetriesThenFails() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer("texto sin viñetas ni citas"));
    Topic t = syllabus.ingest("topic01.pdf", SyllabusServiceTest.validTopic());
    assertThatThrownBy(() -> cheatsheets.cheatsheet(t.getId()))
        .isInstanceOf(SyllabusException.class);
  }
}
