package com.oposiciones.voz;

import com.oposiciones.temario.TemarioException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VozController {

  private final NarracionService narracion;

  public VozController(NarracionService narracion) {
    this.narracion = narracion;
  }

  public record ProgresoDto(Long temaId, Long fragmentoId, double offsetSeg) {}

  @GetMapping("/temas/{id}/narracion")
  public List<NarracionService.ItemPlaylist> narracion(@PathVariable Long id) {
    return narracion.playlist(id);
  }

  @GetMapping(value = "/fragmentos/{fid}/audio", produces = "audio/wav")
  public ResponseEntity<byte[]> audio(@PathVariable Long fid) {
    byte[] wav = narracion.audioDe(fid);
    return ResponseEntity.ok().contentType(MediaType.parseMediaType("audio/wav")).body(wav);
  }

  @GetMapping("/temas/{id}/progreso")
  public ProgresoDto progreso(@PathVariable Long id) {
    var p = narracion.progresoDe(id)
        .orElseThrow(() -> new TemarioException("Sin progreso guardado", 404));
    return new ProgresoDto(p.getTemaId(), p.getFragmentoId(), p.getOffsetSeg());
  }

  @PutMapping("/temas/{id}/progreso")
  public ProgresoDto guardar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
    Object fid = body.get("fragmentoId");
    Object off = body.get("offsetSeg");
    if (fid == null || off == null) throw new TemarioException("Faltan fragmentoId y offsetSeg", 422);
    var p = narracion.guardarProgreso(id, Long.valueOf(fid.toString()),
        Double.parseDouble(off.toString()));
    return new ProgresoDto(p.getTemaId(), p.getFragmentoId(), p.getOffsetSeg());
  }

  @DeleteMapping("/temas/{id}/progreso")
  public ResponseEntity<Void> terminar(@PathVariable Long id) {
    narracion.terminarProgreso(id);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(TemarioException.class)
  public ResponseEntity<Map<String, String>> handle(TemarioException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
