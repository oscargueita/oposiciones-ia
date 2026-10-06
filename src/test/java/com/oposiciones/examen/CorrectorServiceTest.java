package com.oposiciones.examen;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.oposiciones.temario.EmbeddingService;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemarioException;
import com.oposiciones.temario.TemarioService;
import com.oposiciones.temario.TemarioServiceTest;
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
class CorrectorServiceTest {

  @Autowired GeneradorTests generador;
  @Autowired CorrectorService corrector;
  @Autowired TemarioService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  private GeneradorTests.TestCreado creado;

  @BeforeEach
  void setup() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
    when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(
        List.of(new Generation(new AssistantMessage(GeneradorTestsTest.JSON_OK)))));
    Tema t = temario.ingestar("t.pdf", TemarioServiceTest.temaValido());
    creado = generador.generar(t.getId(), 2, Dificultad.MEDIO);
  }

  @Test
  void feedbackInmediatoYNotaFinal() {
    var ps = creado.preguntas();
    var fb1 = corrector.responder(creado.test().getId(), ps.get(0).getId(), 1);
    assertThat(fb1.acierto()).isTrue();
    assertThat(fb1.explicacion()).isNotBlank();
    var fb2 = corrector.responder(creado.test().getId(), ps.get(1).getId(), 0);
    assertThat(fb2.acierto()).isFalse();
    var nota = corrector.finalizar(creado.test().getId());
    assertThat(nota.nota()).isEqualTo(5.0);
    assertThat(nota.aciertos()).isEqualTo(1);
    assertThat(nota.detalle()).hasSize(2);
  }

  @Test
  void sinResponderCuentaComoFalloYRepetirFinalizarFalla() {
    var nota = corrector.finalizar(creado.test().getId());
    assertThat(nota.nota()).isEqualTo(0.0);
    assertThatThrownBy(() -> corrector.finalizar(creado.test().getId()))
        .isInstanceOf(TemarioException.class);
    assertThatThrownBy(
        () -> corrector.responder(creado.test().getId(), creado.preguntas().get(0).getId(), 1))
        .isInstanceOf(TemarioException.class);
  }
}
