package com.examprep.exam;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.examprep.syllabus.Fragment;
import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
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

/** Generates JSON questions with the local chat, validated and with a snapshot per test. */
@Service
public class TestGenerator {

  static final int MAX_REINTENTOS = 3;

  private final TopicRepository topics;
  private final FragmentRepository fragments;
  private final GeneratedTestRepository tests;
  private final QuestionRepository questions;
  private final ChatModel chatModel;
  private final ObjectMapper json;

  public TestGenerator(TopicRepository topics, FragmentRepository fragments,
      GeneratedTestRepository tests, QuestionRepository questions, ChatModel chatModel,
      ObjectMapper json) {
    this.topics = topics;
    this.fragments = fragments;
    this.tests = tests;
    this.questions = questions;
    this.chatModel = chatModel;
    this.json = json;
  }

  public record CreatedTest(GeneratedTest test, List<Question> questions, String notice) {}

  @Transactional
  public CreatedTest generate(Long topicId, int n, Difficulty difficulty) {
    Topic topic = topics.findById(topicId)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + topicId, 404));
    if (topic.getStatus() != Topic.Status.READY) {
      throw new SyllabusException("Tema no listo para generar test", 422);
    }
    if (n < 1 || n > 50) throw new SyllabusException("Pide entre 1 y 50 preguntas", 422);
    List<Fragment> pool = new ArrayList<>(fragments.findByTopicIdOrderBySequenceAsc(topicId));
    if (pool.isEmpty()) throw new SyllabusException("Tema sin contenido para test", 422);
    Collections.shuffle(pool, new Random());
    return buildTest(topicId, "TEMA", pool, n, difficulty);
  }

  @Transactional
  public CreatedTest generateMixed(List<Long> topicIds, int n, Difficulty difficulty) {
    if (n < 1 || n > 50) throw new SyllabusException("Pide entre 1 y 50 preguntas", 422);
    List<Topic> chosen = topicIds == null || topicIds.isEmpty()
        ? topics.findAll().stream().filter(t -> t.getStatus() == Topic.Status.READY).toList()
        : topicIds.stream()
            .map(id -> topics.findById(id)
                .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404)))
            .filter(t -> t.getStatus() == Topic.Status.READY)
            .toList();
    if (chosen.isEmpty()) throw new SyllabusException("Sin temas listos para test mixto", 422);
    // Round-robin equilibrado: una ronda por tema hasta cubrir N
    List<List<Fragment>> perTopic = new ArrayList<>();
    for (Topic t : chosen) {
      List<Fragment> fs = new ArrayList<>(fragments.findByTopicIdOrderBySequenceAsc(t.getId()));
      Collections.shuffle(fs, new Random());
      if (!fs.isEmpty()) perTopic.add(fs);
    }
    if (perTopic.isEmpty()) throw new SyllabusException("Temas sin contenido para test", 422);
    List<Fragment> pool = new ArrayList<>();
    boolean progress = true;
    while (pool.size() < n && progress) {
      progress = false;
      for (List<Fragment> fs : perTopic) {
        if (!fs.isEmpty() && pool.size() < n) {
          pool.add(fs.remove(0));
          progress = true;
        }
      }
    }
    String scope = (topicIds == null || topicIds.isEmpty())
        ? "Todos (" + perTopic.size() + " temas)"
        : "Mixto (" + perTopic.size() + " temas)";
    return buildTest(null, scope, pool, n, difficulty);
  }

  private CreatedTest buildTest(Long topicId, String scope, List<Fragment> pool,
      int n, Difficulty difficulty) {
    int target = Math.min(n, pool.size());
    String notice = target < n ? "Materia para " + target + " de " + n + " pedidas" : null;
    GeneratedTest test = tests.save(new GeneratedTest(topicId, scope, difficulty, target));
    BeanOutputConverter<QuestionJson> converter = new BeanOutputConverter<>(QuestionJson.class);
    List<Question> createdItems = new ArrayList<>();
    Set<Long> used = new LinkedHashSet<>();
    for (Fragment f : pool) {
      if (createdItems.size() >= target || used.size() >= pool.size()) break;
      if (!used.add(f.getId())) continue;
      QuestionJson pj = intentar(f, difficulty, converter);
      if (pj == null) continue;
      try {
        createdItems.add(questions.save(new Question(test.getId(), createdItems.size(), pj.statement(),
            json.writeValueAsString(pj.options()), pj.correctIndex(), pj.explanation(),
            f.getTopicId(), f.getId(), f.getPage())));
      } catch (Exception e) {
        throw new SyllabusException("No se pudo guardar la pregunta", 500, e);
      }
    }
    if (createdItems.isEmpty()) {
      tests.delete(test);
      throw new SyllabusException("No se pudo generar ninguna pregunta válida", 422);
    }
    test.setQuestionCount(createdItems.size());
    return new CreatedTest(test, createdItems, notice);
  }

  private QuestionJson intentar(Fragment f, Difficulty difficulty,
      BeanOutputConverter<QuestionJson> converter) {
    for (int i = 0; i < MAX_REINTENTOS; i++) {
      try {
        var prompt = new PromptTemplate("""
            Genera UNA pregunta tipo test de oposicion a partir del siguiente texto oficial.
            {instruccion}
            La answer correctIndex debe deducirse SOLO del text. Responde SOLO con el JSON: {format}
            TEXTO:
            {text}""").create(Map.of("instruccion", difficulty.getInstruccion(),
            "format", converter.getFormat(), "text", f.getText()));
        String salida = chatModel.call(prompt).getResult().getOutput().getText();
        QuestionJson pj = converter.convert(salida);
        if (isValid(pj)) return pj;
      } catch (Exception ignored) {
        // non-JSON or invalid output: retry
      }
    }
    return null;
  }

  static boolean isValid(QuestionJson pj) {
    if (pj == null || pj.statement() == null || pj.statement().isBlank()) return false;
    if (pj.options() == null || pj.options().size() != 4) return false;
    if (pj.options().stream().anyMatch(o -> o == null || o.isBlank())) return false;
    if (pj.options().stream().map(String::strip).distinct().count() != 4) return false;
    if (pj.correctIndex() < 0 || pj.correctIndex() > 3) return false;
    return pj.explanation() != null && !pj.explanation().isBlank();
  }
}
