# Implementation Plan: Chuletas y Mapas

**Branch**: `005-chuletas-mapas` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/005-chuletas-mapas/spec.md` + stack del usuario: Ollama local, chuleta markdown + mapa Mermaid mindmap persistidos, validación de sintaxis, sin audio.

## Summary

`GET /temas/{id}/chuleta` (markdown con citas) y `GET /temas/{id}/mapa` (mindmap Mermaid 8-15 nodos) generados con `OllamaChatModel`, validados (markdown con citas / sintaxis mindmap propia) con reintento ≤3 y persistidos 1:1 (`chuleta`, `mapa`); invalidación vía `TopicContentDeletedEvent`.

## Technical Context

**Language/Version**: Java 21 + Maven (proyecto existente).

**Primary Dependencies**: Las existentes (Spring AI chat ya en classpath).

**Storage**: Migración `V6__chuletas_mapas.sql` (`chuleta`, `mapa` con FK cascade a temas).

**Testing**: JUnit5 + MockMvc; chat mockeado con markdown/mermaid fijos válido/inválido.

**Target Platform**: macOS/Linux local, offline.

**Project Type**: Monolito modular (paquete `resumen`).

**Performance Goals**: Chuleta + mapa < 2 min (llama3.1:8b local); lectura persistida < 200 ms.

**Constraints**: 100% local; citas obligatorias; TDD; sin audio en esta feature.

**Scale/Scope**: 1 chuleta + 1 mapa por tema; regeneración al reemplazar.

## Constitution Check

- [x] I Local-First: chat localhost. PASS.
- [x] II Java 21 + Boot 3: paquete `resumen`. PASS.
- [x] III RAG cita fuente: chuleta con citas; mapa de conceptos del texto. PASS.
- [x] IV SQLite + Flyway V6. PASS.
- [x] V TTS: fuera de alcance. PASS.
- [x] VI Test-First: chat mockeado. PASS.
- [x] VII Simplicidad: 1 servicio + 2 endpoints, sin colas. PASS.
- [x] VIII English Code: identificadores/comentarios en inglés; mensajes en español. PASS.

Post-Phase-1 re-check: sin violaciones; Complexity Tracking vacía.

## Project Structure

### Documentation (this feature)

```text
specs/005-chuletas-mapas/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── chuletas-mapas-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/examprep/resumen/
├── ResumenService.java (chuleta markdown + validador + persistencia)
├── MapaService.java (mindmap + validador sintáctico + persistencia)
├── ResumenController.java (GET chuleta/mapa)
├── Chuleta.java / Mapa.java + repos
├── ResumenListener.java (invalida en TopicContentDeletedEvent)
└── resources/db/migration/V6__chuletas_mapas.sql
```

**Structure Decision**: Paquete `resumen` nuevo; reutiliza fragmentos de 001 y chat de 004.

## Complexity Tracking

> Sin violaciones — tabla vacía intencionadamente.
