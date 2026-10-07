package com.examprep.resumen;

import com.examprep.syllabus.FragmentRepository;
import com.examprep.syllabus.Topic;
import com.examprep.syllabus.TopicRepository;
import com.examprep.syllabus.SyllabusException;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Map;

/** Hierarchical Mermaid mindmap per topic, persisted 1:1. */
@Service
public class MindmapService {

  static final int MAX_RETRIES = 3;
  static final int MAX_CHARS = 4000;
  static final int MIN_NODES = 8;
  static final int MAX_NODES = 15;

  private final TopicRepository topics;
  private final FragmentRepository fragments;
  private final MindmapRepository mindmaps;
  private final ChatModel chatModel;

  public MindmapService(TopicRepository topics, FragmentRepository fragments,
      MindmapRepository mindmaps, ChatModel chatModel) {
    this.topics = topics;
    this.fragments = fragments;
    this.mindmaps = mindmaps;
    this.chatModel = chatModel;
  }

  @Transactional
  public String mindmap(Long topicId) {
    return mindmaps.findById(topicId)
        .map(Mindmap::getMermaid)
        .orElseGet(() -> generate(topicId));
  }

  private String generate(Long topicId) {
    Topic topic = topics.findById(topicId)
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + topicId, 404));
    if (topic.getStatus() != Topic.Status.READY) {
      throw new SyllabusException("Tema no listo para mapa (estado " + topic.getStatus() + ")", 422);
    }
    var frags = fragments.findByTopicIdOrderBySequenceAsc(topicId);
    if (frags.isEmpty()) throw new SyllabusException("Tema sin contenido para mapa", 422);
    StringBuilder sample = new StringBuilder();
    for (var f : frags) {
      if (sample.length() >= MAX_CHARS) break;
      sample.append("\n\n").append(f.getText());
    }
    for (int i = 0; i < MAX_RETRIES; i++) {
      var prompt = new PromptTemplate("""
          Genera un mapa mental en sintaxis Mermaid mindmap del siguiente texto oficial.
          Reglas estrictas: primera línea exactamente `mindmap`; segunda línea la raíz
          con solo el título del tema (sin prefijos); desarrolla subramas con viñetas
          simples (- o *) indentadas con espacios hasta tener entre 8 y 12 nodos en
          total (resume, no detalles); sin paréntesis, corchetes, llaves,
          almohadillas, dos puntos ni punto y coma en las etiquetas. Basado SOLO en
          el texto. Responde SOLO con el bloque mindmap.
          TEXTO:
          {texto}""").create(Map.of("texto", sample.toString()));
      String raw = chatModel.call(prompt).getResult().getOutput().getText();
      String mm = normalize(raw);
      if (MindmapValidator.valid(mm)) {
        mindmaps.save(new Mindmap(topicId, mm));
        return mm;
      }
    }
    throw new SyllabusException("No se pudo generar un mapa válido", 422);
  }

  /** Normaliza la salida del modelo a un mindmap válido: extrae desde la
   *  línea `mindmap`, quita cercas y prosa, quita viñetas y caracteres
   *  problemáticos, garantiza indentación y topa en 15 nodos. */
  static String normalize(String raw) {
    if (raw == null) return "";
    String s = raw.strip();
    int at = s.indexOf("mindmap");
    if (at >= 0) s = s.substring(at);
    s = s.replaceAll("(?s)\\s*```$", "").strip();
    String[] lines = s.split("\n");
    if (lines.length == 0 || !lines[0].strip().equals("mindmap")) return "";
    StringBuilder out = new StringBuilder("mindmap");
    int nodes = 0;
    for (int i = 1; i < lines.length && nodes < MAX_NODES; i++) {
      String line = lines[i];
      if (line.isBlank()) continue;
      int indent = 0;
      while (indent < line.length() && line.charAt(indent) == ' ') indent++;
      String label = line.substring(indent)
          .replaceAll("^[\\-\\*\\+\\|—–>·•\\s]+", "")
          .replaceAll("[\\(\\)\\[\\]\\{\\}#;:]", "").strip();
      if (label.isEmpty()) continue;
      if (indent == 0) indent = 2;
      out.append("\n").append(" ".repeat(indent)).append(label);
      nodes++;
    }
    return nodes >= MIN_NODES ? out.toString() : "";
  }
}
