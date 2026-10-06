package com.oposiciones.temario;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Divide el texto en chunks de ~2000 caracteres sin partir párrafos.
 * Un chunk NUNCA cruza de página (cita exacta tema+página); el solape
 * es el último párrafo del chunk anterior dentro de la misma página
 * (solo si ≤ 500 caracteres, para no arrastrar párrafos gigantes).
 */
@Component
public class TextChunker {

  public static final int MAX_CHARS = 2000;
  static final int MAX_OVERLAP_CHARS = 500;
  /** Cabeceras de tema al inicio de línea: "TEMA 1", "Tema 12: ..." */
  private static final Pattern TEMA_HEADING = Pattern.compile("(?im)^\\s*TEMA\\s+(\\d+)\\b");

  public record Chunk(int orden, int pagina, String texto) {}

  public List<Chunk> chunk(List<String> pages) {
    List<Chunk> chunks = new ArrayList<>();
    for (int p = 0; p < pages.size(); p++) {
      List<String> paras = new ArrayList<>();
      for (String raw : pages.get(p).split("\\n\\s*\\n")) {
        String t = raw.strip();
        if (!t.isEmpty()) paras.add(t);
      }
      String overlap = null; // se resetea en cada página: sin solapes entre páginas
      int i = 0;
      while (i < paras.size()) {
        StringBuilder cur = new StringBuilder();
        if (overlap != null) cur.append(overlap);
        boolean packedFresh = false;
        while (i < paras.size()) {
          String para = paras.get(i);
          if (packedFresh && cur.length() + 2 + para.length() > MAX_CHARS) break;
          if (cur.length() > 0) cur.append("\n\n");
          cur.append(para);
          packedFresh = true;
          i++;
          if (cur.length() > MAX_CHARS) break; // párrafo gigante: se emite tal cual
        }
        String lastFresh = paras.get(i - 1);
        overlap = lastFresh.length() <= MAX_OVERLAP_CHARS ? lastFresh : null;
        chunks.add(new Chunk(chunks.size(), p + 1, cur.toString()));
      }
    }
    return chunks;
  }

  /** Números de tema distintos declarados como cabecera. >1 ⇒ PDF multi-tema. */
  public TreeSet<Integer> temasDeclarados(String fullText) {
    Matcher m = TEMA_HEADING.matcher(fullText);
    TreeSet<Integer> nums = new TreeSet<>();
    while (m.find()) nums.add(Integer.parseInt(m.group(1)));
    return nums;
  }
}
