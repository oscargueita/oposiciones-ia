# Quickstart: Estudio Tema por Voz (002)

**Prerrequisitos**: 001 funcionando, `say` disponible (macOS), 1 PDF de prueba en `data/temario/`.

## Validación end-to-end

1. Subir tema: `curl -F "files=@data/temario/tema01.pdf" http://localhost:8080/api/v1/temas` → 201.
2. Esperar generación en fondo; comprobar playlist: `curl http://localhost:8080/api/v1/temas/{id}/narracion` → fragmentos en orden con `duracionSeg > 0`.
3. Descargar audio del primer fragmento: `curl http://localhost:8080/api/v1/fragmentos/{fid}/audio -o f1.m4a` → audio reproducible con el texto literal (M4A/AAC desde 2026-10-07; Piper sigue en WAV).
4. Guardar progreso: `curl -X PUT .../temas/{id}/progreso -d '{"fragmentId":N,"offsetSec":12.5}'` → 200.
5. Recuperar progreso: `GET .../temas/{id}/progreso` → mismo fragmento + offset.
6. Borrar progreso (fin): `DELETE .../temas/{id}/progreso` → 204; `GET` posterior → 404.
7. Reemplazar PDF del tema → progreso anterior invalidado (GET → 404).
8. Offline: sin red, repetir 2-5 → todo funciona (`say` es local; Ollama ya descargado).
9. Tests: `./mvnw test` (TtsService mockeado con WAV sintético, sin llamar a `say`).

## Voces

- v1 `say` macOS, voz `Mónica` (nombre EXACTO con acento; `Monica` sin acento cae en voz inglesa sin error).
- Piper opcional: `uv tool install piper-tts` + modelo `es_ES-davefx-medium` en `data/voces/`
  (https://huggingface.co/rhasspy/piper-voices), luego `app.tts.motor=piper`.
  Verificado 2026-10-07: WAV 22050Hz válido, `PiperTtsServiceTest` en verde si hay binario+modelo.

**Esperado**: SC-001–SC-004 de `spec.md`; `data/audio/` regenerable borrándolo.

## Nota 2026-10-07: M4A en vez de WAV

`say` genera M4A/AAC (~10x menos: ~1.5GB los 3600 fragmentos frente a ~16GB en WAV).
`fragmento_audio.formato` (V4) guarda "m4a"/"wav" por fragmento; el endpoint sirve
`audio/mp4` o `audio/wav` según corresponda. Duración M4A vía `afinfo`.
