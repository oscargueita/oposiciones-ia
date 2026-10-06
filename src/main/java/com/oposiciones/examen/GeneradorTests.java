package com.oposiciones.examen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oposiciones.temario.Fragmento;
import com.oposiciones.temario.FragmentoRepository;
import com.oposiciones.temario.Tema;
import com.oposiciones.temario.TemaRepository;
import com.oposiciones.temario.TemarioException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Genera preguntas JSON con el chat local, validadas y con snapshot por test. */
@Service
public class GeneradorTests {

  static final int MAX_REINTENTOS = 3;

  private final TemaRepository temas;
  private final FragmentoRepository fragmentos;
  private final TestGeneradoRepository tests;
  private final PreguntaRepository preguntas;
  private final ChatModel chatModel;
  private final ObjectMapper json;

  public GeneradorTests(TemaRepository temas, FragmentoRepository fragmentos,
      TestGeneradoRepository tests, PreguntaRepository preguntas, ChatModel chatModel,
      ObjectMapper json) {
    this.temas = temas;
    this.fragmentos = fragmentos;
    this.tests = tests;
    this.preguntas = preguntas;
    this.chatModel = chatModel;
    this.json = json;
  }

  public record TestCreado(TestGenerado test, List<Pregunta> preguntas, String aviso) {}

  @Transactional
  public TestCreado generar(Long temaId, int n, Dificultad dificultad) {
    Tema tema = temas.findById(temaId)
        .orElseThrow(() -> new TemarioException("Tema no existe: " + temaId, 404));
    if (tema.getEstado() != Tema.Estado.LISTO) {
      throw new TemarioException("Tema no listo para generar test", 422);
    }
    if (n < 1 || n > 50) throw new TemarioException("Pide entre 1 y 50 preguntas", 422);
    List<Fragmento> base = new ArrayList<>(fragmentos.findByTemaIdOrderByOrdenAsc(temaId));
    if (base.isEmpty()) throw new TemarioException("Tema sin contenido para test", 422);
    Collections.shuffle(base, new Random());
    int objetivo = Math.min(n, base.size());
    String aviso = objetivo < n ? "Materia para " + objetivo + " de " + n + " pedidas" : null;
    TestGenerado test = tests.save(new TestGenerado(temaId, dificultad, objetivo));
    BeanOutputConverter<PreguntaJson> converter = new BeanOutputConverter<>(PreguntaJson.class);
    List<Pregunta> creadas = new ArrayList<>();
    Set<Long> usados = new LinkedHashSet<>();
    for (Fragmento f : base) {
      if (creadas.size() >= objetivo || usados.size() >= base.size()) break;
      if (!usados.add(f.getId())) continue;
      PreguntaJson pj = intentar(f, dificultad, converter);
      if (pj == null) continue;
      try {
        creadas.add(preguntas.save(new Pregunta(test.getId(), creadas.size(), pj.enunciado(),
            json.writeValueAsString(pj.opciones()), pj.correcta(), pj.explicacion(),
            temaId, f.getId(), f.getPagina())));
      } catch (Exception e) {
        throw new TemarioException("No se pudo guardar la pregunta", 500, e);
      }
    }
    if (creadas.isEmpty()) {
      tests.delete(test);
      throw new TemarioException("No se pudo generar ninguna pregunta válida", 422);
    }
    test.setNumPreguntas(creadas.size());
    return new TestCreado(test, creadas, aviso);
  }

  private PreguntaJson intentar(Fragmento f, Dificultad dificultad,
      BeanOutputConverter<PreguntaJson> converter) {
    for (int i = 0; i < MAX_REINTENTOS; i++) {
      try {
        var prompt = new PromptTemplate("""
            Genera UNA pregunta tipo test de oposicion a partir del siguiente texto oficial.
            {instruccion}
            La respuesta correcta debe deducirse SOLO del texto. Responde SOLO con el JSON: {format}
            TEXTO:
            {texto}""").create(Map.of("instruccion", dificultad.getInstruccion(),
            "format", converter.getFormat(), "texto", f.getTexto()));
        String salida = chatModel.call(prompt).getResult().getOutput().getText();
        PreguntaJson pj = converter.convert(salida);
        if (valida(pj)) return pj;
      } catch (Exception ignored) {
        // salida no JSON o inválida: reintento
      }
    }
    return null;
  }

  static boolean valida(PreguntaJson pj) {
    if (pj == null || pj.enunciado() == null || pj.enunciado().isBlank()) return false;
    if (pj.opciones() == null || pj.opciones().size() != 4) return false;
    if (pj.opciones().stream().anyMatch(o -> o == null || o.isBlank())) return false;
    if (pj.opciones().stream().map(String::strip).distinct().count() != 4) return false;
    if (pj.correcta() < 0 || pj.correcta() > 3) return false;
    return pj.explicacion() != null && !pj.explicacion().isBlank();
  }
}
