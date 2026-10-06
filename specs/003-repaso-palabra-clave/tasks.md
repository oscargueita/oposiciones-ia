# Tasks: Repaso por Palabra Clave

**Input**: Design documents from `/specs/003-repaso-palabra-clave/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/repaso-api.yaml, quickstart.md

**Tests**: Incluidos (constitution VI Test-First; embeddings y TTS mockeados).

**Organization**: Por historia de usuario. Sin migración ni setup (reutiliza 001/002).

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Sin infraestructura nueva; verificación de base.

- [x] T001 Verificar que 001/002 compilan y pasan tests (`mvn -B test`) como base de 003

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Normalización y ranking compartidos por todas las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T002 Crear `Normalizador` (minúsculas + NFD sin tildes) en `src/main/java/com/oposiciones/repaso/Normalizador.java`

**Checkpoint**: Foundation ready — normalización probada manualmente.

---

## Phase 3: User Story 1 - Escuchar el fragmento de una palabra clave (Priority: P1) 🎯 MVP

**Goal**: `GET /repasar?q=` devuelve top-5 con cita y audio; vacío si score < 0.35.

**Independent Test**: "recurso de alzada" → top-1 LPAC con audio; "xyzqwerty" → `[]` (quickstart 1+4).

### Tests for User Story 1

> NOTE: Escribir FIRST, verificar FAIL antes de implementar.

- [x] T003 [P] [US1] Test ranking (literal exacto gana, corte 0.35, tilde-insensible) en `src/test/java/com/oposiciones/repaso/RepasoServiceTest.java`
- [x] T004 [P] [US1] Test contrato `GET /repasar` (200, `[]`, 422) en `src/test/java/com/oposiciones/repaso/RepasoControllerTest.java` (MockMvc)

### Implementation for User Story 1

- [x] T005 [US1] Implementar `RepasoService` (0.5 coseno + 0.5 bonus literal, corte 0.35, topK=5) en `src/main/java/com/oposiciones/repaso/RepasoService.java` (depende de T002)
- [x] T006 [US1] Implementar `GET /api/v1/repasar` (q 2-500 chars, 422 si inválida) en `src/main/java/com/oposiciones/repaso/RepasoController.java`

**Checkpoint**: US1 funciona sola — quickstart pasos 1+4 en verde.

---

## Phase 4: User Story 2 - Elegir entre varios resultados (Priority: P2)

**Goal**: Candidatos ordenados con cita y audio cada uno.

**Independent Test**: "plazo" → 5 ordenados de varios temas, segundo escuchable (quickstart 2+5).

### Tests for User Story 2

- [x] T007 [P] [US2] Test orden por relevancia y audioUrl por candidato en `src/test/java/com/oposiciones/repaso/RepasoServiceTest.java`

### Implementation for User Story 2

- [x] T008 [US2] Incluir cita (tema+posición) y `audioUrl` en cada candidato en `src/main/java/com/oposiciones/repaso/RepasoService.java`

**Checkpoint**: US1 + US2 funcionan; quickstart pasos 2+5 en verde.

---

## Phase 5: User Story 3 - Acotar el repaso a un tema (Priority: P3)

**Goal**: `temaId` filtra antes de rankear; 404 si no existe.

**Independent Test**: "plazo" + temaId=2 → solo Constitución (quickstart 3).

### Tests for User Story 3

- [x] T009 [P] [US3] Test acotado por tema y 404 en `src/test/java/com/oposiciones/repaso/RepasoControllerTest.java` (MockMvc)

### Implementation for User Story 3

- [x] T010 [US3] Implementar filtro `temaId` en `src/main/java/com/oposiciones/repaso/RepasoService.java` y 404 en `src/main/java/com/oposiciones/repaso/RepasoController.java`

**Checkpoint**: Las 3 historias funcionan; quickstart paso 3 en verde.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Validación completa.

- [x] T011 Ejecutar validación completa `quickstart.md` pasos 1-8 y `mvn -B test` en verde

---

## Dependencies & Execution Order

- T001 → T002 (BLOCKS) → US1 (T003-T006, MVP) → US2 (T007-T008) → US3 (T009-T010) → T011.
- Tests FAIL primero en cada historia.

### Parallel Opportunities

- [P] T003+T004, T007+T009 (ficheros de test distintos; T009 toca ControllerTest existente — secuencial con T004 si coincide fichero: T004 crea, T009 amplía → orden T004→T009).
- T007 (servicio) en paralelo con T009 tras T004.

## Implementation Strategy

**MVP**: T001-T006 → STOP, validar quickstart 1+4.
**Incremental**: +US2 → +US3 → polish.
