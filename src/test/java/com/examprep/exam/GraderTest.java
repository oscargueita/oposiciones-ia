package com.examprep.exam;

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
class GraderTest {

  @Autowired TestGenerator generador;
  @Autowired Grader corrector;
  @Autowired SyllabusService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  private TestGenerator.CreatedTest created;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(TestGeneratorTest.JSON_OK)))));
    Topic t = temario.ingest("t.pdf", SyllabusServiceTest.validTopic());
    created = generador.generate(t.getId(), 2, Difficulty.MEDIUM);
  }

  @Test
  void immediateFeedbackAndFinalGrade() {
    var ps = created.questions();
    var fb1 = corrector.answer(created.test().getId(), ps.get(0).getId(), 1);
    assertThat(fb1.correct()).isTrue();
    assertThat(fb1.explanation()).isNotBlank();
    var fb2 = corrector.answer(created.test().getId(), ps.get(1).getId(), 0);
    assertThat(fb2.correct()).isFalse();
    var score = corrector.finish(created.test().getId());
    assertThat(score.score()).isEqualTo(5.0);
    assertThat(score.correctCount()).isEqualTo(1);
    assertThat(score.details()).hasSize(2);
  }

  @Test
  void unansweredCountsAsMissAndRefinishFails() {
    var score = corrector.finish(created.test().getId());
    assertThat(score.score()).isEqualTo(0.0);
    assertThatThrownBy(() -> corrector.finish(created.test().getId()))
        .isInstanceOf(SyllabusException.class);
    assertThatThrownBy(
        () -> corrector.answer(created.test().getId(), created.questions().get(0).getId(), 1))
        .isInstanceOf(SyllabusException.class);
  }
}
