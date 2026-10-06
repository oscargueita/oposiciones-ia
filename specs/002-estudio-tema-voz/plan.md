# Implementation Plan: Estudio Tema por Voz

**Branch**: `002-estudio-tema-voz` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-estudio-tema-voz/spec.md` + stack del usuario: TtsService desacoplado + audio por fragmento + progreso con offset; `say` macOS v1, Piper opcional.

## Summary

Narración por voz de un tema como audios WAV por fragmento (orden 001): `TtsService` con `SayTtsService` por defecto y `PiperTtsService` tras property; generación en segundo plano al publicarse `TemaListoEvent` con fallback perezoso; progreso (fragmento + segundo) persistido con invalidación en reemplazo/borrado; 4 endpoints REST.

## Technical Context

**Language/Version**: Java 21 + Maven (proyecto 001 existente).

**Primary Dependencies**: Las de 001 + ninguna nueva obligatoria (`say` es binario del SO; Piper solo binario + modelo externos).

**Storage**: `data/audio/{temaId}/{fragmentoId}.wav` (WAV 22050Hz mono 16-bit, gitignored) + tablas `fragmento_audio`, `progreso_escucha` (migración `V2__estudio_voz.sql`).

**Testing**: JUnit5 + MockMvc; `TtsService` mockeado con WAV sintético (sin invocar `say`); `@Async` sincronizado en tests vía ejecutor directo.

**Target Platform**: macOS local (say); Linux con Piper tras configurar binario+modelo.

**Project Type**: Monolito modular (paquete `voz` nuevo).

**Performance Goals**: Playlist inmediata si audios pregenerados; `GET audio` < 1 s si existe, generación bajo demanda solo como fallback; progreso PUT/GET < 200 ms.

**Constraints**: 100% local; `TtsService` desacoplado (constitution V); TDD; no romper 001 (evento, no llamadas directas).

**Scale/Scope**: ~1200 fragmentos ≈ 240MB WAV; regenerable borrando `data/audio/`.

## Constitution Check

- [x] I Local-First: say/Piper + WAV + SQLite, todo en máquina. PASS.
- [x] II Java 21 + Boot 3: paquete `voz`, servicios Spring. PASS.
- [x] III RAG: reutiliza fragmentos literales de 001, sin cambios. PASS.
- [x] IV SQLite + Flyway V2. PASS.
- [x] V TTS desacoplado: interfaz + 2 implementaciones tras property. PASS.
- [x] VI Test-First: WAV sintético mockeado, tests por historia. PASS.
- [x] VII Simplicidad: `@Async` + evento en vez de job scheduler. PASS.

Post-Phase-1 re-check: sin violaciones; Complexity Tracking vacía.

## Project Structure

### Documentation (this feature)

```text
specs/002-estudio-tema-voz/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── estudio-voz-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/oposiciones/voz/
├── TtsService.java (interfaz: Audio sintetizar(texto))
├── SayTtsService.java (@Primary; ProcessBuilder say)
├── PiperTtsService.java (@ConditionalOnProperty app.tts.motor=piper)
├── AudioStore.java (rutas data/audio, lectura/escritura WAV)
├── NarracionService.java (playlist, lazy-gen, progreso CRUD)
├── VozController.java (REST según contracts/estudio-voz-api.yaml)
├── TemaListoListener.java (@Async @EventListener genera audios)
└── ProgresoEscucha.java + repos + V2__estudio_voz.sql
src/main/java/com/oposiciones/temario/TemarioService.java (publica TemaListoEvent)
```

**Structure Decision**: Paquete `voz` nuevo en el monolito; 001 solo añade el evento (sin dependencia a voz).

## Complexity Tracking

> Sin violaciones — tabla vacía intencionadamente.
