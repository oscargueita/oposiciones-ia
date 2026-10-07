package com.examprep.syllabus;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class SyllabusController {

  private final SyllabusService service;
  private final EmbeddingService embeddingService;

  public SyllabusController(SyllabusService service, EmbeddingService embeddingService) {
    this.service = service;
    this.embeddingService = embeddingService;
  }

  public record TopicDto(Long id, String title, String sourceName, int pageCount,
      String status, String errorMessage, long fragmentCount) {}
  public record FragmentDto(Long id, int sequence, int page, String text) {}
  public record SearchResultDto(Long fragmentId, Long topicId, int page, String text, double score) {}

  private TopicDto dto(SyllabusService.TopicView v) {
    var t = v.topic();
    return new TopicDto(t.getId(), t.getTitle(), t.getSourceName(), t.getPageCount(),
        t.getStatus().name(), t.getErrorMessage(), v.fragmentCount());
  }

  @GetMapping("/temas")
  public List<TopicDto> list() {
    return service.list().stream().map(this::dto).toList();
  }

  @GetMapping("/temas/{id}")
  public TopicDto details(@PathVariable Long id) {
    var vista = service.list().stream().filter(v -> v.topic().getId().equals(id)).findFirst()
        .orElseThrow(() -> new SyllabusException("Tema no existe: " + id, 404));
    return dto(vista);
  }

  @PostMapping("/temas")
  public ResponseEntity<List<TopicDto>> upload(@RequestParam("files") List<MultipartFile> files) throws Exception {
    if (files == null || files.isEmpty()) throw new SyllabusException("Sin ficheros", 422);
    List<TopicDto> creados = new java.util.ArrayList<>();
    for (MultipartFile f : files) {
      Topic t = service.ingest(f.getOriginalFilename(), f.getBytes());
      long n = service.fragmentsOf(t.getId()).size();
      creados.add(new TopicDto(t.getId(), t.getTitle(), t.getSourceName(), t.getPageCount(),
          t.getStatus().name(), null, n));
    }
    return ResponseEntity.status(201).body(creados);
  }

  @PutMapping("/temas/{id}/pdf")
  public TopicDto replace(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
    Topic t = service.replace(id, file.getOriginalFilename(), file.getBytes());
    long n = service.fragmentsOf(t.getId()).size();
    return new TopicDto(t.getId(), t.getTitle(), t.getSourceName(), t.getPageCount(),
        t.getStatus().name(), null, n);
  }

  @PatchMapping("/temas/{id}/titulo")
  public TopicDto rename(@PathVariable Long id, @RequestBody Map<String, String> body) {
    Topic t = service.rename(id, body.get("title"));
    long n = service.fragmentsOf(t.getId()).size();
    return new TopicDto(t.getId(), t.getTitle(), t.getSourceName(), t.getPageCount(),
        t.getStatus().name(), null, n);
  }

  @DeleteMapping("/temas/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/temas/{id}/fragmentos")
  public List<FragmentDto> fragments(@PathVariable Long id,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
    var allItems = service.fragmentsOf(id);
    int from = Math.min(page * size, allItems.size());
    int to = Math.min(from + size, allItems.size());
    return allItems.subList(from, to).stream()
        .map(f -> new FragmentDto(f.getId(), f.getSequence(), f.getPage(), f.getText()))
        .toList();
  }

  @GetMapping("/buscar")
  public List<SearchResultDto> search(@RequestParam("q") String q,
      @RequestParam(defaultValue = "5") int topK) {
    return embeddingService.search(q, topK).stream()
        .map(s -> new SearchResultDto(s.fragment().getId(), s.fragment().getTopicId(),
            s.fragment().getPage(), s.fragment().getText(), s.score()))
        .toList();
  }

  @ExceptionHandler(SyllabusException.class)
  public ResponseEntity<Map<String, String>> handle(SyllabusException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
