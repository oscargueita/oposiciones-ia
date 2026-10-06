# Tasks: Estudio Tema por Voz

**Input**: Design documents from `/specs/002-estudio-tema-voz/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/estudio-voz-api.yaml, quickstart.md

**Tests**: Incluidos (constitution VI Test-First NON-NEGOTIABLE; `TtsService` mockeado con WAV sintético, sin invocar `say`).

**Organization**: Por historia de usuario para entrega incremental e independiente.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Propiedades TTS + async + gitignore de audio.

- [x] T001 Añadir propiedades `app.tts.*` (motor=say, voz, binarios, `data/audio`) en `src/main/resources/application.properties`
- [x] T002 [P] Habilitar `@EnableAsync` + ejecutor en `src/main/java/com/oposiciones/config/AsyncConfig.java`
- [x] T003 [P] Añadir `data/audio/` a `.gitignore` y crear `data/audio/.gitkeep`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persistencia voz + interfaz TTS + evento 001 que bloquean todas las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T004 Crear migración `src/main/resources/db/migration/V2__estudio_voz.sql` (tablas fragmento_audio y progreso_escucha con FK cascade)
- [x] T005 [P] Crear entidades `AudioFragmento` y `ProgresoEscucha` en `src/main/java/com/oposiciones/voz/` ("offset_seg REAL >= 0", "fragmento_id debe pertenecer al tema")
- [x] T006 [P] Crear repositorios en `src/main/java/com/oposiciones/voz/` (AudioFragmentoRepository, ProgresoRepository)
- [x] T007 Crear interfaz `TtsService` + `Audio` (WAV 22050Hz mono 16-bit) en `src/main/java/com/oposiciones/voz/TtsService.java`
- [x] T008 Crear `SayTtsService` (`@Primary`, `ProcessBuilder say`) en `src/main/java/com/oposiciones/voz/SayTtsService.java`
- [x] T009 Crear `AudioStore` (rutas `data/audio/{temaId}/{fragmentoId}.wav`, lectura/escritura) en `src/main/java/com/oposiciones/voz/AudioStore.java`
- [x] T010 Publicar `TemaListoEvent` al pasar a LISTO en `src/main/java/com/oposiciones/temario/TemarioService.java` (sin dependencia a voz)

**Checkpoint**: Foundation ready — `mvn test` compila, V2 migra en arranque real.

---

## Phase 3: User Story 1 - Escuchar un tema entero (Priority: P1) 🎯 MVP

**Goal**: Playlist ordenada + audio WAV por fragmento, generado en fondo tras ingesta.

**Independent Test**: Subir PDF → playlist con duraciones > 0 → primer WAV reproducible con texto literal (quickstart 2-3).

### Tests for User Story 1

> NOTE: Escribir FIRST, verificar FAIL antes de implementar.

- [x] T011 [P] [US1] Test generación en fondo + playlist ordenada con `TtsService` mockeado en `src/test/java/com/oposiciones/voz/NarracionServiceTest.java`
- [x] T012 [P] [US1] Test contrato `GET /temas/{id}/narracion` + `GET /fragmentos/{fid}/audio` en `src/test/java/com/oposiciones/voz/VozControllerTest.java` (MockMvc)

### Implementation for User Story 1

- [x] T013 [US1] Implementar `NarracionService` (playlist, generación por fragmento, lazy fallback) en `src/main/java/com/oposiciones/voz/NarracionService.java` (depende de T005-T009)
- [x] T014 [US1] Implementar `TemaListoListener` (`@Async @EventListener` genera WAV en orden) en `src/main/java/com/oposiciones/voz/TemaListoListener.java`
- [x] T015 [US1] Implementar `GET /temas/{id}/narracion` + `GET /fragmentos/{fid}/audio` (422 si tema vacío/en proceso/en error) en `src/main/java/com/oposiciones/voz/VozController.java`

**Checkpoint**: US1 funciona sola — quickstart pasos 2-3 en verde.

---

## Phase 4: User Story 2 - Pausar, reanudar y reiniciar (Priority: P2)

**Goal**: Progreso fragmento+segundo con PUT/GET/DELETE; reanudar en segundo exacto.

**Independent Test**: Guardar offset 12.5 → recuperarlo idéntico; borrar → 404 (quickstart 4-6).

### Tests for User Story 2

- [x] T016 [P] [US2] Test PUT/GET/DELETE progreso + validación (fragmento del tema, offset ≤ duración) en `src/test/java/com/oposiciones/voz/ProgresoServiceTest.java`
- [x] T017 [P] [US2] Test contrato `GET/PUT/DELETE /temas/{id}/progreso` en `src/test/java/com/oposiciones/voz/VozControllerTest.java` (MockMvc)

### Implementation for User Story 2

- [x] T018 [US2] Implementar CRUD progreso en `src/main/java/com/oposiciones/voz/NarracionService.java` (offset_seg REAL >= 0)
- [x] T019 [US2] Implementar `GET/PUT/DELETE /temas/{id}/progreso` en `src/main/java/com/oposiciones/voz/VozController.java`

**Checkpoint**: US1 + US2 funcionan; quickstart pasos 4-6 en verde.

---

## Phase 5: User Story 3 - Continuar donde lo dejó (Priority: P3)

**Goal**: Progreso sobrevive a reinicios; se invalida al reemplazar/borrar el tema.

**Independent Test**: Reemplazar PDF → GET progreso 404; borrar tema → sin rastro (quickstart 7).

### Tests for User Story 3

- [x] T020 [P] [US3] Test invalidación en reemplazo + cascada en borrado en `src/test/java/com/oposiciones/voz/ProgresoServiceTest.java`

### Implementation for User Story 3

- [x] T021 [US3] Borrar progreso en `reemplazar` y extender `borrarContenido` (progreso + audios del tema) en `src/main/java/com/oposiciones/temario/TemarioService.java`
- [x] T022 [US3] Borrar ficheros `data/audio/{temaId}` al reemplazar/borrar en `src/main/java/com/oposiciones/voz/AudioStore.java`

**Checkpoint**: Las 3 historias funcionan; quickstart paso 7 en verde.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Piper opcional + validación offline completa.

- [x] T023 [P] Implementar `PiperTtsService` (`@ConditionalOnProperty app.tts.motor=piper`) en `src/main/java/com/oposiciones/voz/PiperTtsService.java`
- [x] T024 Verificar offline (sin red, pasos 2-5) y documentar voz `say` usada en `specs/002-estudio-tema-voz/quickstart.md`
- [x] T025 Ejecutar validación completa `quickstart.md` pasos 1-9 y `./mvnw test` en verde

---

## Dependencies & Execution Order

- Setup (T001-T003) → Foundational (T004-T010, BLOCKS) → US1 (T011-T015, MVP) → US2 (T016-T019) → US3 (T020-T022) → Polish (T023-T025).
- US2/US3 secuenciales por defecto (comparten `NarracionService`).
- En cada historia: tests FAIL primero → servicio → endpoints.

### Parallel Opportunities

- [P] T002, T003, T005, T006 (ficheros distintos).
- Tests de cada historia en paralelo entre sí (T011+T012, T016+T017).
- T023 (Piper) en paralelo con cualquier fase (fichero aislado tras T007).

## Implementation Strategy

**MVP**: Fases 1+2+3 (T001-T015) → STOP, validar quickstart 2-3, primera escucha real.
**Incremental**: +US2 (progreso exacto) → +US3 (invalidación) → polish Piper/offline.
