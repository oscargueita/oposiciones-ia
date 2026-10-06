package com.oposiciones.examen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integración opcional contra Ollama real. Se omite sin servidor.
 * Manual: {@code mvn -Dtest=GeneradorOllamaIT test} con Ollama + llama3.1:8b.
 */
@SpringBootTest
@Transactional
class GeneradorOllamaIT {

  @Autowired GeneradorTests generador;
  @Autowired TemarioService temario;

  @Test
  void generaPreguntaReal() {
    assumeTrue(ollamaDisponible(), "Ollama no disponible");
    Tema t = temario.ingestar("t.pdf", com.oposiciones.temario.TemarioServiceTest.temaValido());
    var creado = generador.generar(t.getId(), 1, Dificultad.MEDIO);
    assertThat(creado.preguntas()).hasSize(1);
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
