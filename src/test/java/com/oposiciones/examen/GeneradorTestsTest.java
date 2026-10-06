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
class GeneradorTestsTest {

  @Autowired GeneradorTests generador;
  @Autowired TemarioService temario;
  @MockBean EmbeddingService embeddingService;
  @MockBean ChatModel chatModel;

  static final String JSON_OK = """
      {"enunciado":"Según el texto, ¿cuál es el plazo?","opciones":["diez días","un mes","tres meses","un año"],"correcta":1,"explicacion":"El texto fija un mes."}""";

  private void stubBase() {
    when(embeddingService.embed(anyString())).thenReturn(new float[]{1});
    when(embeddingService.encode(org.mockito.ArgumentMatchers.any(float[].class)))
        .thenCallRealMethod();
  }

  private ChatResponse respuesta(String texto) {
    return new ChatResponse(List.of(new Generation(new AssistantMessage(texto))));
  }

  private Tema temaConContenido() {
    return temario.ingestar("t.pdf", TemarioServiceTest.temaValido());
  }

  @Test
  void generaConCitaYDificultad() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta(JSON_OK));
    Tema t = temaConContenido();
    var creado = generador.generar(t.getId(), 2, Dificultad.MEDIO);
    assertThat(creado.preguntas()).hasSize(2);
    assertThat(creado.test().getDificultad()).isEqualTo(Dificultad.MEDIO);
    assertThat(creado.preguntas().get(0).getCitaTemaId()).isEqualTo(t.getId());
    assertThat(creado.aviso()).isNull();
  }

  @Test
  void jsonInvalidoReintentaYSiTodoFalla422() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta("no es json"));
    Tema t = temaConContenido();
    assertThatThrownBy(() -> generador.generar(t.getId(), 1, Dificultad.FACIL))
        .isInstanceOf(TemarioException.class);
  }

  @Test
  void masPreguntasQueFragmentosRecortaConAviso() {
    stubBase();
    when(chatModel.call(any(Prompt.class))).thenReturn(respuesta(JSON_OK));
    Tema t = temaConContenido();
    var creado = generador.generar(t.getId(), 50, Dificultad.FACIL);
    assertThat(creado.preguntas().size()).isLessThan(50);
    assertThat(creado.aviso()).contains("Materia para");
  }
}
