package com.examprep.exam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.examprep.syllabus.EmbeddingService;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.ContentHash;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import com.examprep.syllabus.SyllabusService;
import com.examprep.syllabus.SyllabusServiceTest;
import com.examprep.syllabus.TestPdf;
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
class TestGeneratorTest {

  @Autowired TestGenerator generator;
  @Autowired SyllabusService syllabus;
  @Autowired TopicRepository topics;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  static final String JSON_OK = """
      {"statement":"Según el text, ¿cuál es el plazo?","options":["diez días","un mes","tres meses","un año"],"correctIndex":1,"explanation":"El text fija un mes."}""";

  private void stubBase() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  private ChatResponse answer(String text) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
  }

  private Topic topicWithContent() {
    return syllabus.ingest("t.pdf", SyllabusServiceTest.validTopic());
  }

  @Test
  void generatesWithCitationAndDifficulty() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(JSON_OK));
    Topic t = topicWithContent();
    var created = generator.generate(t.getId(), 2, Difficulty.MEDIUM);
    assertThat(created.questions()).hasSize(2);
    assertThat(created.test().getDifficulty()).isEqualTo(Difficulty.MEDIUM);
    assertThat(created.questions().get(0).getCitedTopicId()).isEqualTo(t.getId());
    assertThat(created.notice()).isNull();
  }

  @Test
  void jsonInvalidoReintentaYSiTodoFalla422() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer("no es json"));
    Topic t = topicWithContent();
    assertThatThrownBy(() -> generator.generate(t.getId(), 1, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void moreQuestionsThanFragmentsTrimsWithNotice() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(JSON_OK));
    Topic t = topicWithContent();
    var created = generator.generate(t.getId(), 50, Difficulty.EASY);
    assertThat(created.questions().size()).isLessThan(50);
    assertThat(created.notice()).contains("Materia para");
  }

  @Test
  void generatesMixedBalancedAcrossTopics() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(JSON_OK));
    Topic a = topicWithContent();
    Topic b = syllabus.ingest("topic-b.pdf",
        TestPdf.ofPages("TEMA 9. Otro\nContenido distinto del otro tema con texto suficiente para el test mixto."));
    var created = generator.generateMixed(List.of(a.getId(), b.getId()), 4, Difficulty.MEDIUM);
    assertThat(created.questions()).hasSize(4);
    assertThat(created.test().getTopicId()).isNull();
    assertThat(created.test().getScope()).contains("Mixto");
    assertThat(created.questions().stream().map(Question::getCitedTopicId).distinct()).hasSize(2);
  }

  @Test
  void generatesMixedFromAllWithoutList() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(answer(JSON_OK));
    topicWithContent();
    var created = generator.generateMixed(null, 2, Difficulty.EASY);
    assertThat(created.questions()).hasSize(2);
    assertThat(created.test().getScope()).startsWith("Todos");
  }

  @Test
  void questionCountOutOfRangeRejected() {
    stubBase();
    Topic t = topicWithContent();
    assertThatThrownBy(() -> generator.generate(t.getId(), 0, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
    assertThatThrownBy(() -> generator.generate(t.getId(), 51, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
    assertThatThrownBy(() -> generator.generateMixed(null, 0, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void emptyReadyTopicRejected() {
    stubBase();
    Topic t = topics.save(new Topic("empty", "empty.pdf",
        ContentHash.of("3".repeat(64)), 1));
    t.markReady();
    assertThatThrownBy(() -> generator.generate(t.getId(), 2, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void mixedUnknownTopicRejected() {
    stubBase();
    assertThatThrownBy(() -> generator.generateMixed(List.of(999999L), 2, Difficulty.EASY))
        .isInstanceOf(SyllabusException.class);
  }
}
