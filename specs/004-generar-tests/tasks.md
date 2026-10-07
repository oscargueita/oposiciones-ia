# Tasks: Generar Tests

**Input**: Design documents from `/specs/004-generar-tests/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/tests-api.yaml, quickstart.md

**Tests**: Incluidos (constitution VI Test-First; `OllamaChatModel` mockeado).

**Organization**: Por historia de usuario. Paquete `examen`.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Sin dependencias nuevas; base verificada.

- [x] T001 Verificar base (`mvn -B test` en verde) como punto de partida de 004

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persistencia + generación validada que bloquean las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T002 Crear migración `src/main/resources/db/migration/V3__tests.sql` (test_generado, pregunta con opciones JSON, respuesta)
- [x] T003 [P] Crear entidades `TestGenerado`, `Pregunta` ("opciones JSON array de exactamente 4", "correcta 0-3"), `Respuesta` en `src/main/java/com/oposiciones/examen/`
- [x] T004 [P] Crear repositorios en `src/main/java/com/oposiciones/examen/` (TestGeneradoRepository, PreguntaRepository, RespuestaRepository)
- [x] T005 Crear `Dificultad` (3 plantillas) + `PreguntaJson` (record conversor) en `src/main/java/com/oposiciones/examen/`
- [x] T006 Crear `GeneradorTests` (muestreo sin reemplazo, chat JSON, validador, reintento ≤3, aviso si N recortada) en `src/main/java/com/oposiciones/examen/GeneradorTests.java`

**Checkpoint**: Foundation ready — genera preguntas válidas con chat mockeado.

---

## Phase 3: User Story 1 - Generar un test de un tema (Priority: P1) 🎯 MVP

**Goal**: `POST /temas/{id}/tests?n=&dificultad=` con preguntas citadas.

**Independent Test**: 3 preguntas MEDIO de Constitución con 4 opciones y cita (quickstart 1-2).

### Tests for User Story 1

> NOTE: Escribir FIRST, verificar FAIL antes de implementar.

- [x] T007 [P] [US1] Test generación (JSON válido/inválido, reintento, N recortada, dificultad guardada) en `src/test/java/com/oposiciones/examen/GeneradorTestsTest.java`
- [x] T008 [P] [US1] Test contrato `POST /temas/{id}/tests` (201, 422) en `src/test/java/com/oposiciones/examen/ExamenControllerTest.java` (MockMvc)

### Implementation for User Story 1

- [x] T009 [US1] Implementar `POST /api/v1/temas/{id}/tests` en `src/main/java/com/oposiciones/examen/ExamenController.java` (depende de T002-T006)

**Checkpoint**: US1 funciona sola — quickstart pasos 1-2 en verde.

---

## Phase 4: User Story 2 - Responder con feedback y nota (Priority: P2)

**Goal**: Respuesta inmediata por pregunta + nota al finalizar.

**Independent Test**: 7/10 con feedback por pregunta y nota 7.0 (quickstart 3-4).

### Tests for User Story 2

- [x] T010 [P] [US2] Test responder (acierto/fallo+explicación) y finalizar (nota, sin-responder=falla) en `src/test/java/com/oposiciones/examen/CorrectorServiceTest.java`
- [x] T011 [P] [US2] Test contrato `POST /tests/{id}/responder` + `/finalizar` en `src/test/java/com/oposiciones/examen/ExamenControllerTest.java` (MockMvc)

### Implementation for User Story 2

- [x] T012 [US2] Implementar `CorrectorService` (upsert respuesta, feedback, nota 10·aciertos/total) en `src/main/java/com/oposiciones/examen/CorrectorService.java`
- [x] T013 [US2] Implementar endpoints responder/finalizar en `src/main/java/com/oposiciones/examen/ExamenController.java`

**Checkpoint**: US1 + US2 funcionan; quickstart pasos 3-4 en verde.

---

## Phase 5: User Story 3 - Historial por tema (Priority: P3)

**Goal**: `GET /temas/{id}/tests` con notas ordenadas.

**Independent Test**: 2 intentos visibles con fecha y nota (quickstart 5).

### Tests for User Story 3

- [x] T014 [P] [US3] Test historial ordenado + tema sin intentos en `src/test/java/com/oposiciones/examen/ExamenControllerTest.java` (MockMvc)

### Implementation for User Story 3

- [x] T015 [US3] Implementar `GET /api/v1/temas/{id}/tests` en `src/main/java/com/oposiciones/examen/ExamenController.java`

**Checkpoint**: Las 3 historias funcionan; quickstart paso 5 en verde.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Robustez LLM real + validación completa.

- [x] T016 Test de integración opcional contra Ollama real (skipped sin servidor) en `src/test/java/com/examprep/exam/OllamaQuestionGenerationIT.java`
- [x] T017 Ejecutar validación completa `quickstart.md` pasos 1-9 y `mvn -B test` en verde

## Dependencies & Execution Order

- T001 → Foundational (T002-T006, BLOCKS) → US1 (T007-T009, MVP) → US2 (T010-T013) → US3 (T014-T015) → Polish (T016-T017).
- En cada historia: tests FAIL primero → implementación.

### Parallel Opportunities

- [P] T003+T004 (ficheros distintos).
- Tests por historia en paralelo (T007+T008, T010+T011).
- T016 aislado en cualquier momento.

## Implementation Strategy

**MVP**: T001-T009 → STOP, validar quickstart 1-2.
**Incremental**: +US2 → +US3 → polish.

---

## Addendum: Tests mixtos (2026-10-07, FR-011)

- [x] T018 V7 (`tema_id` anulable + `alcance`) en `src/main/resources/db/migration/V7__tests_mixtos.sql`
- [x] T019 `generateMixed` round-robin + `POST /api/v1/tests` + `GET /api/v1/tests` en `src/main/java/com/examprep/exam/`
- [x] T020 Tests servicio + contrato mixto + historial global
- [x] T021 UI multi-tema (un tema / varios / todos) + historial global en `frontend/src/views/Tests.tsx`
- [x] T022 Validación E2E mixto real + suite verde
