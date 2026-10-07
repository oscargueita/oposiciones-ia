# Tasks: Chuletas y Mapas

**Input**: Design documents from `/specs/005-chuletas-mapas/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/chuletas-mapas-api.yaml, quickstart.md

**Tests**: Incluidos (constitution VI Test-First; `OllamaChatModel` mockeado).

**Organization**: Por historia de usuario. Paquete `resumen`.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Sin dependencias nuevas; base verificada.

- [x] T001 Verificar base (`mvn -B test` en verde) como punto de partida de 005

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persistencia + listener de invalidación que bloquean las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T002 Crear migración `src/main/resources/db/migration/V6__chuletas_mapas.sql` (tablas chuleta y mapa 1:1 con FK cascade)
- [x] T003 [P] Crear entidades `Cheatsheet` y `Mindmap` ("markdown no vacío", "mermaid con bloque mindmap") + repositorios en `src/main/java/com/examprep/resumen/`
- [x] T004 Crear `ResumenListener` (borra chuleta+mapa en `TopicContentDeletedEvent`) en `src/main/java/com/examprep/resumen/ResumenListener.java`

**Checkpoint**: Foundation ready — V6 migra y la invalidación funciona.

---

## Phase 3: User Story 1 - Generar chuleta de un tema (Priority: P1) 🎯 MVP

**Goal**: `GET /temas/{id}/chuleta` con markdown citado y persistido.

**Independent Test**: Chuleta de la Constitución con secciones y citas; segunda llamada idéntica sin regenerar (quickstart 1-2).

### Tests for User Story 1

> NOTE: Escribir FIRST, verificar FAIL antes de implementar.

- [x] T005 [P] [US1] Test generación (markdown válido/inválido, reintento, persistencia e invalidación) en `src/test/java/com/examprep/resumen/CheatsheetServiceTest.java`
- [x] T006 [P] [US1] Test contrato `GET /temas/{id}/chuleta` (200, 404, 422) en `src/test/java/com/examprep/resumen/ResumenControllerTest.java` (MockMvc)

### Implementation for User Story 1

- [x] T007 [US1] Implementar `CheatsheetService` (prompt, validador markdown+citas, reintento ≤3, 1:1) en `src/main/java/com/examprep/resumen/CheatsheetService.java` (depende de T002-T003)
- [x] T008 [US1] Implementar `GET /api/v1/temas/{id}/chuleta` en `src/main/java/com/examprep/resumen/ResumenController.java`

**Checkpoint**: US1 funciona sola — quickstart pasos 1-2 en verde.

---

## Phase 4: User Story 2 - Generar mapa conceptual (Priority: P2)

**Goal**: `GET /temas/{id}/mapa` con mindmap 8-15 nodos validado.

**Independent Test**: Mapa válido renderizable en mermaid.live (quickstart 3-4).

### Tests for User Story 2

- [x] T009 [P] [US2] Test generación (mindmap válido/inválido, 8-15 nodos, persistencia) en `src/test/java/com/examprep/resumen/MindmapServiceTest.java`
- [x] T010 [P] [US2] Test contrato `GET /temas/{id}/mapa` en `src/test/java/com/examprep/resumen/ResumenControllerTest.java` (MockMvc)

### Implementation for User Story 2

- [x] T011 [US2] Implementar `MindmapService` + `MindmapValidator` (mindmap, indentación, 8-15 nodos, paréntesis) en `src/main/java/com/examprep/resumen/`
- [x] T012 [US2] Implementar `GET /api/v1/temas/{id}/mapa` en `src/main/java/com/examprep/resumen/ResumenController.java`

**Checkpoint**: US1 + US2 funcionan; quickstart pasos 3-4 en verde.

---

## Phase 5: User Story 3 - Exportar para estudiar offline (Priority: P3)

**Goal**: Los GET ya devuelven texto reutilizable descargable.

**Independent Test**: Guardar ambas respuestas en ficheros y abrirlos con herramientas estándar (quickstart US3).

### Tests for User Story 3

- [x] T013 [P] [US3] Test content-type texto en ambos endpoints en `src/test/java/com/examprep/resumen/ResumenControllerTest.java` (MockMvc)

### Implementation for User Story 3

- [x] T014 [US3] Servir chuleta como `text/markdown` y mapa como `text/plain` en `src/main/java/com/examprep/resumen/ResumenController.java`

**Checkpoint**: Las 3 historias funcionan; quickstart completo en verde.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validación completa.

- [x] T015 Ejecutar validación completa `quickstart.md` pasos 1-8 y `mvn -B test` en verde

---

## Dependencies & Execution Order

- T001 → Foundational (T002-T004, BLOCKS) → US1 (T005-T008, MVP) → US2 (T009-T012) → US3 (T013-T014) → T015.
- En cada historia: tests FAIL primero → implementación.

### Parallel Opportunities

- [P] T003 + tests por historia en ficheros distintos (T005+T006, T009+T010).

## Implementation Strategy

**MVP**: T001-T008 → STOP, validar quickstart 1-2.
**Incremental**: +US2 → +US3 → polish.
