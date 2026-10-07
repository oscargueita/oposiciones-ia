# Quickstart: Repaso por Palabra Clave (003)

**Prerrequisitos**: App con temario TAI cargado (001) y audios 002.

## Validación end-to-end

1. `curl "http://localhost:8080/api/v1/repasar?q=recurso%20de%20alzada"` → top-1 de la LPAC con cita y `audioUrl`.
2. `curl "http://localhost:8080/api/v1/repasar?q=plazo"` → 5 candidatos ordenados de varios temas.
3. `curl "http://localhost:8080/api/v1/repasar?q=plazo&topicId=2"` → solo Constitución.
4. `curl "http://localhost:8080/api/v1/repasar?q=xyzqwerty"` → `[]` (sin resultados, sin audio).
5. Escuchar el top-1: `curl <audioUrl del 1> -o repaso.wav` → WAV con el texto literal.
6. `curl "http://localhost:8080/api/v1/repasar?q=a"` → 422 (mínimo 2 caracteres).
7. Offline: repetir 1-4 sin red → idéntico.
8. Tests: `mvn -B test` (embeddings y TTS mockeados).

**Esperado**: SC-001–SC-004 de `spec.md`.
