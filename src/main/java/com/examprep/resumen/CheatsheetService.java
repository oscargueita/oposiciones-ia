package com.examprep.resumen;

import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/** One-page markdown cheatsheet per topic, persisted 1:1. */
@Service
public class CheatsheetService {

  static final int MAX_RETRIES = 3;
  static final int MAX_CHARS = 8000;

  private final TopicRepository topics;
  private final FragmentRepository fragments;
  private final CheatsheetRepository cheatsheets;
  private final ChatModel chatModel;

  public CheatsheetService(TopicRepository topics, FragmentRepository fragments,
      CheatsheetRepository cheatsheets, ChatModel chatModel) {
    this.topics = topics;
    this.fragments = fragments;
    this.cheatsheets = cheatsheets;
    this.chatModel = chatModel;
  }

  @Transactional
  public String cheatsheet(Long topicId) {
    return cheatsheets.findById(topicId)
        .map(Cheatsheet::getMarkdown)
        .orElseGet(() -> generate(topicId));
  }

  private String generate(Long topicId) {
    Topic topic = topics.findById(topicId)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + topicId, 404));
    if (topic.getStatus() != Topic.Status.READY) {
      throw new SyllabusException("Tema no listo para chuleta (estado " + topic.getStatus() + ")", 422);
    }
    var frags = fragments.findByTopicIdOrderBySequenceAsc(topicId);
    if (frags.isEmpty()) throw new SyllabusException("Tema sin contenido para chuleta", 422);
    StringBuilder sample = new StringBuilder();
    for (var f : frags) {
      if (sample.length() >= MAX_CHARS) break;
      sample.append("\n\n[pág. ").append(f.getPage()).append("]\n").append(f.getText());
    }
    for (int i = 0; i < MAX_RETRIES; i++) {
      var prompt = new PromptTemplate("""
          Resume el siguiente texto oficial en una chuleta de UNA página en markdown:
          secciones con ##, viñetas con conceptos clave, cifras y plazos.
          Cada sección debe citar su procedencia como (tema, pág. N).
          No inventes nada fuera del texto. Responde SOLO con el markdown.
          TEXTO:
          {texto}""").create(Map.of("texto", sample.toString()));
      String md = chatModel.call(prompt).getResult().getOutput().getText();
      if (valid(md)) {
        cheatsheets.save(new Cheatsheet(topicId, md.strip()));
        return md.strip();
      }
    }
    throw new SyllabusException("No se pudo generar una chuleta válida", 422);
  }

  static boolean valid(String md) {
    if (md == null || md.isBlank()) return false;
    long bullets = md.lines().filter(l -> {
      String t = l.strip();
      return t.startsWith("- ") || t.startsWith("* ") || t.matches("\\d+\\. .*");
    }).count();
    return bullets >= 5 && md.contains("pág");
  }
}
