package com.examprep.resumen;

import com.examprep.syllabus.SyllabusException;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ResumenController {

  private final CheatsheetService cheatsheets;
  private final MindmapService mindmaps;

  public ResumenController(CheatsheetService cheatsheets, MindmapService mindmaps) {
    this.cheatsheets = cheatsheets;
    this.mindmaps = mindmaps;
  }

  @GetMapping(value = "/temas/{id}/chuleta", produces = "text/markdown;charset=UTF-8")
  public ResponseEntity<String> chuleta(@PathVariable Long id) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/markdown;charset=UTF-8"))
        .body(cheatsheets.cheatsheet(id));
  }

  @GetMapping(value = "/temas/{id}/mapa", produces = "text/plain;charset=UTF-8")
  public ResponseEntity<String> mapa(@PathVariable Long id) {
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/plain;charset=UTF-8"))
        .body(mindmaps.mindmap(id));
  }

  @ExceptionHandler(SyllabusException.class)
  public ResponseEntity<Map<String, String>> handle(SyllabusException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
