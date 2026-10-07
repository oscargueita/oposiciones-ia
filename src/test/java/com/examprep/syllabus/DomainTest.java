package com.examprep.syllabus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.examprep.exam.Answer;
import com.examprep.exam.GeneratedTest;
import com.examprep.exam.Difficulty;
import com.examprep.exam.Question;
import com.examprep.exam.Score;
import com.examprep.voice.ListeningProgress;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Dominio puro, sin Spring: invariantes de agregados y value objects. */
class DomainTest {

  @Test
  void contentHashValidatesShape() {
    assertThatThrownBy(() -> ContentHash.of("xyz")).isInstanceOf(IllegalArgumentException.class);
    assertThat(ContentHash.of("0".repeat(64)).value()).hasSize(64);
  }

  @Test
  void topicProtectsInvariants() {
    Topic t = new Topic("T", "t.pdf", ContentHash.of("0".repeat(64)), 3);
    assertThatThrownBy(() -> t.rename("  ")).isInstanceOf(SyllabusException.class);
    t.rename("Nuevo");
    assertThat(t.getTitle()).isEqualTo("Nuevo");
    t.markReady();
    assertThat(t.isReady()).isTrue();
    assertThatThrownBy(() -> t.beginReplacement(ContentHash.of("0".repeat(64)), 3))
        .isInstanceOf(SyllabusException.class);
  }

  @Test
  void fragmentValidates() {
    assertThatThrownBy(() -> new Fragment(1L, -1, 1, "x")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Fragment(1L, 0, 0, "x")).isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new Fragment(1L, 0, 1, "  ")).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void scoreMathAndGrade() {
    assertThat(Score.of(7, 10).value()).isEqualTo(7.0);
    assertThat(Score.of(0, 0).value()).isEqualTo(0.0);
    GeneratedTest test = new GeneratedTest(1L, Difficulty.MEDIUM, 2);
    org.springframework.test.util.ReflectionTestUtils.setField(test, "id", 1L);
    Question q1 = new Question(1L, 0, "E?", "[\"a\",\"b\",\"c\",\"d\"]", 1, "exp", 1L, 1L, 1);
    Question q2 = new Question(1L, 1, "E?", "[\"a\",\"b\",\"c\",\"d\"]", 0, "exp", 1L, 2L, 1);
    org.springframework.test.util.ReflectionTestUtils.setField(q1, "id", 1L);
    org.springframework.test.util.ReflectionTestUtils.setField(q2, "id", 2L);
    Score s = test.grade(List.of(q1, q2), Map.of(1L, new Answer(q1, 1)));
    assertThat(s.value()).isEqualTo(5.0);
    test.finish();
    assertThat(test.isGraded()).isTrue();
    assertThatThrownBy(test::finish).isInstanceOf(SyllabusException.class);
  }

  @Test
  void progressValidatesOffset() {
    ListeningProgress p = new ListeningProgress(1L, 5L, 0);
    p.moveTo(5L, 3.5, 10.0);
    assertThat(p.getOffsetSec()).isEqualTo(3.5);
    assertThatThrownBy(() -> p.moveTo(5L, 99.0, 10.0)).isInstanceOf(SyllabusException.class);
  }
}
