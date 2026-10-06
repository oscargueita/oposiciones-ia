# Quickstart: Estudio Tema por Voz (002)

**Prerrequisitos**: 001 funcionando, `say` disponible (macOS), 1 PDF de prueba en `data/temario/`.

## Validación end-to-end

1. Subir tema: `curl -F "files=@data/temario/tema01.pdf" http://localhost:8080/api/v1/temas` → 201.
2. Esperar generación en fondo; comprobar playlist: `curl http://localhost:8080/api/v1/temas/{id}/narracion` → fragmentos en orden con `duracionSeg > 0`.
3. Descargar audio del primer fragmento: `curl http://localhost:8080/api/v1/fragmentos/{fid}/audio -o f1.wav` → WAV reproducible con el texto literal.
4. Guardar progreso: `curl -X PUT .../temas/{id}/progreso -d '{"fragmentoId":N,"offsetSeg":12.5}'` → 200.
5. Recuperar progreso: `GET .../temas/{id}/progreso` → mismo fragmento + offset.
6. Borrar progreso (fin): `DELETE .../temas/{id}/progreso` → 204; `GET` posterior → 404.
7. Reemplazar PDF del tema → progreso anterior invalidado (GET → 404).
8. Offline: sin red, repetir 2-5 → todo funciona (say es local).
9. Tests: `./mvnw test` (TtsService mockeado con WAV sintético, sin llamar a `say`).

**Esperado**: SC-001–SC-004 de `spec.md`; `data/audio/` regenerable borrándolo.
