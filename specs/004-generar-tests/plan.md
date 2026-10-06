# Implementation Plan: Generar Tests

**Branch**: `004-generar-tests` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-generar-tests/spec.md` + stack del usuario: Ollama llama3.1:8b con JSON estructurado + validación, snapshot por test, dificultad en prompt.

## Summary

Generación de tests tipo examen con `OllamaChatModel` + `BeanOutputConverter`: 1 pregunta JSON por fragmento muestreado, validador (4 opciones, correcta, cita) con reintento ≤3, snapshot en `pregunta`, corrección inmediata por pregunta + nota al finalizar, historial por tema. Dificultad vía 3 plantillas de prompt.

## Technical Context

**Language/Version**: Java 21 + Maven (proyecto existente).

**Primary Dependencies**: Las existentes (Spring AI chat ya en classpath por starter Ollama).

**Storage**: Migración `V3__tests.sql` (`test_generado`, `pregunta` con opciones JSON, `respuesta`).

**Testing**: JUnit5 + MockMvc; `OllamaChatModel` mockeado con JSON fijo válido/inválido; 1 test de integración opcional contra Ollama real (skipped sin servidor).

**Target Platform**: macOS/Linux local, offline (modelo descargado).

**Project Type**: Monolito modular (paquete `test` → `examen` para evitar choque con `src/test`).

**Performance Goals**: Generar 10 preguntas < 3 min (llama3.1:8b local ~10-15 s/pregunta con reintentos); responder/finalizar < 500 ms.

**Constraints**: 100% local; snapshot obligatorio; TDD; sin negativos v1; test mixto diferido.

**Scale/Scope**: Tests de 1-50 preguntas; historial sin límite práctico en SQLite.

## Constitution Check

- [x] I Local-First: chat Ollama localhost. PASS.
- [x] II Java 21 + Boot 3: paquete `examen`. PASS.
- [x] III RAG cita fuente: pregunta generada desde fragmento + cita guardada; validador anti-invención. PASS.
- [x] IV SQLite + Flyway V3. PASS.
- [x] V TTS: fuera de alcance. PASS.
- [x] VI Test-First: chat mockeado + JSON válido/inválido. PASS.
- [x] VII Simplicidad: 1 servicio generador + 1 corrector, sin colas. PASS.

Post-Phase-1 re-check: sin violaciones; Complexity Tracking vacía.

## Project Structure

### Documentation (this feature)

```text
specs/004-generar-tests/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── tests-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/oposiciones/examen/
├── Dificultad.java (FACIL/MEDIO/DIFICIL + plantilla prompt)
├── PreguntaJson.java (record para BeanOutputConverter)
├── GeneradorTests.java (muestreo + chat + validador + reintentos)
├── CorrectorService.java (responder inmediato + finalizar/nota)
├── ExamenController.java (REST según contracts/tests-api.yaml)
├── TestGenerado.java / Pregunta.java / Respuesta.java + repos
└── resources/db/migration/V3__tests.sql
```

**Structure Decision**: Paquete `examen` (evita confusión con `src/test` y con `repaso`).

## Complexity Tracking

> Sin violaciones — tabla vacía intencionadamente.
