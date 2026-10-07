package com.examprep.syllabus;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Splits the text into ~2000-character chunks without breaking paragraphs.
 * A chunk NEVER crosses pages (exact topic+page citation); the overlap
 * is the last paragraph of the previous chunk within the same page
 * (only if ≤ 500 characters, to avoid dragging huge paragraphs).
 */
@Component
public class TextChunker {

  public static final int MAX_CHARS = 2000;
  static final int MAX_OVERLAP_CHARS = 500;
  /** Topic headers at line start: "TEMA 1", "Topic 12: ..." */
  private static final Pattern TEMA_HEADING = Pattern.compile("(?im)^\\s*TEMA\\s+(\\d+)\\b");

  public record Chunk(int sequence, int page, String text) {}

  public List<Chunk> chunk(List<String> pages) {
    List<Chunk> chunks = new ArrayList<>();
    for (int p = 0; p < pages.size(); p++) {
      List<String> paragraphs = new ArrayList<>();
      for (String raw : pages.get(p).split("\\n\\s*\\n")) {
        String t = raw.strip();
        if (!t.isEmpty()) paragraphs.add(t);
      }
      String overlap = null; // reset on each page: no overlap across pages
      int i = 0;
      while (i < paragraphs.size()) {
        StringBuilder cur = new StringBuilder();
        if (overlap != null) cur.append(overlap);
        boolean packedFresh = false;
        while (i < paragraphs.size()) {
          String paragraph = paragraphs.get(i);
          if (packedFresh && cur.length() + 2 + paragraph.length() > MAX_CHARS) break;
          if (cur.length() > 0) cur.append("\n\n");
          cur.append(paragraph);
          packedFresh = true;
          i++;
          if (cur.length() > MAX_CHARS) break; // giant paragraph: emitted as-is
        }
        String lastFresh = paragraphs.get(i - 1);
        overlap = lastFresh.length() <= MAX_OVERLAP_CHARS ? lastFresh : null;
        chunks.add(new Chunk(chunks.size(), p + 1, cur.toString()));
      }
    }
    return chunks;
  }

  /** Distinct topic numbers declared as headers. >1 ⇒ multi-topic PDF. */
  public TreeSet<Integer> declaredTopics(String fullText) {
    Matcher m = TEMA_HEADING.matcher(fullText);
    TreeSet<Integer> nums = new TreeSet<>();
    while (m.find()) nums.add(Integer.parseInt(m.group(1)));
    return nums;
  }
}
