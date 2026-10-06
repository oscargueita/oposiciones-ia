# Implementation Plan: Repaso por Palabra Clave

**Branch**: `003-repaso-palabra-clave` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-repaso-palabra-clave/spec.md` + stack del usuario: top-5 híbrido literal+embeddings sobre 001, audio 002 bajo demanda, sin persistencia nueva.

## Summary

`GET /api/v1/repasar?q=&temaId?&topK=`: ranking híbrido en memoria (0.5 coseno + 0.5 bonus literal con normalización sin tildes, corte 0.35), top-5 con cita y `audioUrl` de 002 (genera si falta). Sin migración ni entidades nuevas.

## Technical Context

**Language/Version**: Java 21 + Maven (proyecto existente).

**Primary Dependencies**: Las de 001/002, ninguna nueva.

**Storage**: Sin cambios (lectura de `fragmentos` + `chunk_embedding`; audios vía 002).

**Testing**: JUnit5 + MockMvc; embeddings y TTS mockeados; casos: literal exacto gana, tilde-insensible, sin resultados, acotado, q inválida.

**Target Platform**: macOS/Linux local, offline.

**Project Type**: Monolito modular (paquete `repaso` nuevo).

**Performance Goals**: Repaso completo < 2 s sobre ~3600 vectores (brute-force en memoria); primer audio bajo demanda según 002.

**Constraints**: 100% local; 0 alucinaciones (lista vacía si score < 0.35); TDD; no tocar 001/002 salvo reutilizar.

**Scale/Scope**: Válido hasta ~50k chunks (entonces índice); hoy ~3600.

## Constitution Check

- [x] I Local-First: solo lectura local + motores locales. PASS.
- [x] II Java 21 + Boot 3: paquete `repaso`, servicio testeable. PASS.
- [x] III RAG cita fuente: todo candidato cita tema+posición; corte anti-alucinación. PASS.
- [x] IV Sin migración nueva. PASS.
- [x] V TTS: reutiliza `NarracionService.audioDe`, sin acoples nuevos. PASS.
- [x] VI Test-First. PASS.
- [x] VII Simplicidad: 1 servicio + 1 endpoint, sin persistencia. PASS.

Post-Phase-1 re-check: sin violaciones; Complexity Tracking vacía.

## Project Structure

### Documentation (this feature)

```text
specs/003-repaso-palabra-clave/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── repaso-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/oposiciones/repaso/
├── RepasoService.java (normalizar, ranking híbrido, corte 0.35, topK)
└── RepasoController.java (GET /api/v1/repasar; 422/404)
src/test/... (RepasoServiceTest + RepasoControllerTest)
```

**Structure Decision**: Paquete `repaso` nuevo; reutiliza `FragmentoRepository`, `EmbeddingService`, `NarracionService` (solo lectura/audio).

## Complexity Tracking

> Sin violaciones — tabla vacía intencionadamente.
