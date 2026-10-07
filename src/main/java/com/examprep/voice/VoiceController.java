package com.examprep.voice;

import com.examprep.syllabus.SyllabusException;
import java.util.List;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VoiceController {

  private final NarrationService narration;

  public VoiceController(NarrationService narration) {
    this.narration = narration;
  }

  public record ProgressDto(Long topicId, Long fragmentId, double offsetSec) {}

  @GetMapping("/temas/{id}/narracion")
  public List<NarrationService.PlaylistItem> narration(@PathVariable Long id) {
    return narration.playlist(id);
  }

  @GetMapping(value = "/fragmentos/{fid}/audio")
  public ResponseEntity<byte[]> audio(@PathVariable Long fid) {
    var delivery = narration.audioOf(fid);
    String contentType = "m4a".equals(delivery.format()) ? "audio/mp4" : "audio/wav";
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(delivery.data());
  }

  @GetMapping("/temas/{id}/progreso")
  public ProgressDto progress(@PathVariable Long id) {
    var p = narration.progressOf(id)
        .orElseThrow(() -> new SyllabusException("Sin progreso guardado", 404));
    return new ProgressDto(p.getTopicId(), p.getFragmentId(), p.getOffsetSec());
  }

  @PutMapping("/temas/{id}/progreso")
  public ProgressDto save(@PathVariable Long id, @RequestBody Map<String, Object> body) {
    Object fid = body.get("fragmentId");
    Object off = body.get("offsetSec");
    if (fid == null || off == null) throw new SyllabusException("Faltan fragmentId y offsetSec", 422);
    var p = narration.saveProgress(id, Long.valueOf(fid.toString()),
        Double.parseDouble(off.toString()));
    return new ProgressDto(p.getTopicId(), p.getFragmentId(), p.getOffsetSec());
  }

  @DeleteMapping("/temas/{id}/progreso")
  public ResponseEntity<Void> finish(@PathVariable Long id) {
    narration.clearProgress(id);
    return ResponseEntity.noContent().build();
  }

  @ExceptionHandler(SyllabusException.class)
  public ResponseEntity<Map<String, String>> handle(SyllabusException e) {
    return ResponseEntity.status(e.getStatus()).body(Map.of("error", e.getMessage()));
  }
}
