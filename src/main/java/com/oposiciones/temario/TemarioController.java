package com.oposiciones.temario;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class TemarioController {

  private final TemarioService service;
  private final EmbeddingService embeddingService;

  public TemarioController(TemarioService service, EmbeddingService embeddingService) {
    this.service = service;
    this.embeddingService = embeddingService;
  }

  public record TemaDto(Long id, String titulo, String origenNombre, int numPaginas,
      String estado, String mensajeError, long numFragmentos) {}
  public record FragmentoDto(Long id, int orden, int pagina, String texto) {}
  public record BusquedaDto(Long fragmentoId, Long temaId, int pagina, String texto, double score) {}

  private TemaDto dto(TemarioService.TemaVista v) {
    var t = v.tema();
    return new TemaDto(t.getId(), t.getTitulo(), t.getOrigenNombre(), t.getNumPaginas(),
        t.getEstado().name(), t.getMensajeError(), v.numFragmentos());
  }

  @GetMapping("/temas")
  public List<TemaDto> listar() {
    return service.listar().stream().map(this::dto).toList();
  }

  @GetMapping("/temas/{id}")
  public TemaDto detalle(@PathVariable Long id) {
    var vista = service.listar().stream().filter(v -> v.tema().getId().equals(id)).findFirst()
        .orElseThrow(() -> new TemarioException("Tema no existe: " + id, 404));
    return dto(vista);
  }

  @PostMapping("/temas")
  public ResponseEntity<List<TemaDto>> subir(@RequestParam("files") List<MultipartFile> files) throws Exception {
    if (files == null || files.isEmpty()) throw new TemarioException("Sin ficheros", 422);
    List<TemaDto> creados = new java.util.ArrayList<>();
    for (MultipartFile f : files) {
      Tema t = service.ingestar(f.getOriginalFilename(), f.getBytes());
      long n = service.fragmentosDe(t.getId()).size();
      creados.add(new TemaDto(t.getId(), t.getTitulo(), t.getOrigenNombre(), t.getNumPaginas(),
          t.getEstado().name(), null, n));
    }
    return ResponseEntity.status(201).body(creados);
  }

  @PutMapping("/temas/{id}/pdf")
  public TemaDto reemplazar(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws Exception {
    Tema t = service.reemplazar(id, file.getOriginalFilename(), file.getBytes());
    long n = service.fragmentosDe(t.getId()).size();
    return new TemaDto(t.getId(), t.getTitulo(), t.getOrigenNombre(), t.getNumPaginas(),
        t.getEstado().name(), null, n);
  }

  @PatchMapping("/temas/{id}/titulo")
  public TemaDto renombrar(@PathVariable Long id, @RequestBody Map<String, String> body) {
    Tema t = service.renombrar(id, body.get("titulo"));
    long n = service.fragmentosDe(t.getId()).size();
    return new TemaDto(t.getId(), t.getTitulo(), t.getOrigenNombre(), t.getNumPaginas(),
        t.getEstado().name(), null, n);
  }

  @DeleteMapping("/temas/{id}")
  public ResponseEntity<Void> borrar(@PathVariable Long id) {
    service.borrar(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/temas/{id}/fragmentos")
  public List<FragmentoDto> fragmentos(@PathVariable Long id,
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size) {
    var todos = service.fragmentosDe(id);
    int from = Math.min(page * size, todos.size());
    int to = Math.min(from + size, todos.size());
    return todos.subList(from, to).stream()
        .map(f -> new FragmentoDto(f.getId(), f.getOrden(), f.getPagina(), f.getTexto()))
        .toList();
  }

  @GetMapping("/buscar")
  public List<BusquedaDto> buscar(@RequestParam("q") String q,
      @RequestParam(defaultValue = "5") int topK) {
    return embeddingService.search(q, topK).stream()
        .map(s -> new BusquedaDto(s.fragmento().getId(), s.fragmento().getTemaId(),
            s.fragmento().getPagina(), s.fragmento().getTexto(), s.score()))
        .toList();
  }

  @ExceptionHandler(TemarioException.class)
  public ResponseEntity<Map<String, String>> handle(TemarioException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
