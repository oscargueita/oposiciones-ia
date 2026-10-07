# Tasks: Frontend React

**Input**: Design documents from `/specs/006-frontend-react/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/ui-api.yaml, quickstart.md

**Tests**: Vitest + Testing Library para componentes clave (player, test). Sin Playwright en v1 (validación manual quickstart).

**Organization**: Por historia de usuario. Carpeta `frontend/`.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Toolchain Node + scaffold Vite.

- [x] T001 Instalar Node 22 LTS (fnm o paquete oficial) y verificar `node --version`
- [x] T002 Crear scaffold `frontend/` (Vite 6 + React 18 + TS + Router + Vitest) con proxy `/api → 8080`
- [x] T003 [P] Añadir dependencias `react-markdown` y `mermaid` en `frontend/package.json`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Base compartida (API client, layout, servido por Boot) que bloquea las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T004 Crear cliente `fetch` tipado de la API en `frontend/src/api.ts`
- [x] T005 Crear `App.tsx` con Router (`/temas`, `/repaso`, `/tests`, `/material`) y menú en `frontend/src/App.tsx`
- [x] T006 Configurar Boot para servir `frontend/dist` (copia en build + SPA fallback) y `.gitignore` de `dist/` y `static/`

**Checkpoint**: Foundation ready — :5173 lista temas reales vía proxy.

---

## Phase 3: User Story 1 - Estudiar un tema escuchándolo (Priority: P1) 🎯 MVP

**Goal**: Lista + subida + reproductor con autoavance y continuar.

**Independent Test**: Subir PDF, reproducir 3 fragmentos, pausar, recargar y continuar (quickstart 3-5).

### Tests for User Story 1

> NOTE: Vitest FIRST donde aporte.

- [x] T007 [P] [US1] Test `Player` (autoavance al `ended`, guarda progreso al pausar) en `frontend/src/components/Player.test.tsx`

### Implementation for User Story 1

- [x] T008 [US1] Implementar vista Temas (lista + subida multipart) en `frontend/src/views/Temas.tsx`
- [x] T009 [US1] Implementar `Player.tsx` (`<audio>`, playlist, autoavance, seek desde progreso) en `frontend/src/components/Player.tsx`
- [x] T010 [US1] Implementar vista Estudio (playlist + Player + continuar) en `frontend/src/views/Estudio.tsx`

**Checkpoint**: US1 funciona sola — quickstart pasos 3-5 en verde.

---

## Phase 4: User Story 2 - Repasar por palabra clave (Priority: P2)

**Goal**: Búsqueda con 5 candidatos y audio.

**Independent Test**: "plazo" → 5 citas escuchables (quickstart 6).

### Tests for User Story 2

- [x] T011 [P] [US2] Test vista Repaso (render de candidatos y estados vacío/error) en `frontend/src/views/Repaso.test.tsx`

### Implementation for User Story 2

- [x] T012 [US2] Implementar vista Repaso (query, temaId, candidatos + audio) en `frontend/src/views/Repaso.tsx`

**Checkpoint**: US1 + US2 funcionan; quickstart paso 6 en verde.

---

## Phase 5: User Story 3 - Hacer tests con nota (Priority: P3)

**Goal**: Generar, 1x1 con feedback, nota e historial.

**Independent Test**: Test de 3 con nota e historial (quickstart 7).

### Tests for User Story 3

- [x] T013 [P] [US3] Test flujo test (pregunta, feedback, nota) en `frontend/src/views/Tests.test.tsx`

### Implementation for User Story 3

- [x] T014 [US3] Implementar vista Tests (generar, 1x1, feedback, nota, historial) en `frontend/src/views/Tests.tsx`

**Checkpoint**: US1-US3 funcionan; quickstart paso 7 en verde.

---

## Phase 6: User Story 4 - Ver chuleta y mapa (Priority: P4)

**Goal**: Chuleta renderizada + mapa dibujado.

**Independent Test**: Ver ambos de un tema (quickstart 8).

### Tests for User Story 4

- [x] T015 [P] [US4] Test vista Material (render markdown + diagrama) en `frontend/src/views/Material.test.tsx`

### Implementation for User Story 4

- [x] T016 [US4] Implementar vista Material (`react-markdown` + `mermaid.render`) en `frontend/src/views/Material.tsx`

**Checkpoint**: Las 4 historias funcionan; quickstart paso 8 en verde.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Build integrado + validación completa.

- [x] T017 Build `npm run build` servido por Boot en :8080 sin :5173 (quickstart 9)
- [x] T018 Ejecutar validación completa `quickstart.md` pasos 1-10
- [x] T019 Specs Playwright (navegación, flujo estudio autolimpieza, repaso) + fixture PDF en `frontend/e2e/`
- [x] T020 Fallback SPA en Boot (`SpaController`) para rutas directas

---

## Dependencies & Execution Order

- T001-T003 → Foundational (T004-T006, BLOCKS) → US1 (T007-T010, MVP) → US2 (T011-T012) → US3 (T013-T014) → US4 (T015-T016) → Polish (T017-T018).
- Tests Vitest FIRST en cada historia.

### Parallel Opportunities

- [P] T003 con T002; tests por historia en paralelo (distintos ficheros).
- US2/US3/US4 secuenciales por defecto (comparten api.ts/App estables, podrían paralelizarse con equipo).

## Implementation Strategy

**MVP**: T001-T010 → STOP, validar quickstart 3-5.
**Incremental**: +US2 → +US3 → +US4 → polish.
