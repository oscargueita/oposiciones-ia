package com.examprep.review;

import com.examprep.syllabus.SyllabusException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

  private final ReviewService repaso;

  public ReviewController(ReviewService repaso) {
    this.repaso = repaso;
  }

  @GetMapping("/repasar")
  public List<ReviewService.Candidate> review(@RequestParam("q") String q,
      @RequestParam(value = "topicId", required = false) Long topicId,
      @RequestParam(value = "topK", required = false, defaultValue = "5") int topK) {
    return repaso.review(q, topicId, topK);
  }

  @ExceptionHandler(SyllabusException.class)
  public ResponseEntity<Map<String, String>> handle(SyllabusException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
