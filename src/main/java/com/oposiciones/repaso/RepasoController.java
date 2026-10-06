package com.oposiciones.repaso;

import com.oposiciones.temario.TemarioException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class RepasoController {

  private final RepasoService repaso;

  public RepasoController(RepasoService repaso) {
    this.repaso = repaso;
  }

  @GetMapping("/repasar")
  public List<RepasoService.Candidato> repasar(@RequestParam("q") String q,
      @RequestParam(value = "temaId", required = false) Long temaId,
      @RequestParam(value = "topK", required = false, defaultValue = "5") int topK) {
    return repaso.repasar(q, temaId, topK);
  }

  @ExceptionHandler(TemarioException.class)
  public ResponseEntity<Map<String, String>> handle(TemarioException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
