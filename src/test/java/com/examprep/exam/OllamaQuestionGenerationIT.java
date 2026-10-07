package com.examprep.exam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.examprep.syllabus.Topic;
import com.examprep.syllabus.SyllabusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Optional integration against real Ollama. Skipped without a server.
 * Manual: {@code mvn -Dtest=TestGeneratorOllamaIT test} with Ollama + llama3.1:8b.
 */
@SpringBootTest
@Transactional
class OllamaQuestionGenerationIT {

  @Autowired TestGenerator generator;
  @Autowired SyllabusService syllabus;

  @Test
  void generatesRealQuestion() {
    assumeTrue(ollamaDisponible(), "Ollama no disponible");
    Topic t = syllabus.ingest("t.pdf", com.examprep.syllabus.SyllabusServiceTest.validTopic());
    var created = generator.generate(t.getId(), 1, Difficulty.MEDIUM);
    assertThat(created.questions()).hasSize(1);
  }

  private static boolean ollamaDisponible() {
    try {
      var c = (java.net.HttpURLConnection) new java.net.URI("http://localhost:11434/api/tags")
          .toURL().openConnection();
      c.setConnectTimeout(2000);
      return c.getResponseCode() == 200;
    } catch (Exception e) {
      return false;
    }
  }
}
