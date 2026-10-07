package com.examprep.resumen;

/** Dependency-free Mermaid mindmap syntax check. */
public final class MindmapValidator {
  private MindmapValidator() {}

  public static boolean valid(String mm) {
    if (mm == null || mm.isBlank()) return false;
    String[] lines = mm.strip().split("\n");
    if (lines.length < 2 || !lines[0].strip().equals("mindmap")) return false;
    int nodes = 0;
    int depth = 0;
    for (int i = 1; i < lines.length; i++) {
      String line = lines[i];
      if (line.isBlank()) continue;
      int indent = 0;
      while (indent < line.length() && line.charAt(indent) == ' ') indent++;
      if (indent == 0) return false; // root only on first line
      // tolerate bullets and tree-drawing prefixes (-, *, +, |, —, >)
      String label = line.substring(indent).replaceAll("^[\\-\\*\\+\\|—–>·•\\s]+", "").strip();
      if (label.isEmpty()) continue;
      if (label.matches(".*[\\(\\)\\[\\]\\{\\}#;].*")) return false;
      nodes++;
      depth = Math.max(depth, indent);
    }
    if (depth == 0) return false;
    return nodes >= MindmapService.MIN_NODES && nodes <= MindmapService.MAX_NODES;
  }
}
