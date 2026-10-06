# Tasks: Ingesta PDF Temario

**Input**: Design documents from `/specs/001-ingesta-pdf-temario/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/temario-api.yaml, quickstart.md

**Tests**: Incluidos (constitution VI Test-First NON-NEGOTIABLE: tests FIRST, FAIL antes de implementar).

**Organization**: Por historia de usuario para entrega incremental e independiente.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Proyecto Maven Spring Boot 3 + dependencias base.

- [x] T001 Crear proyecto Maven Spring Boot 3.3 Java 21 con `spring-ai-bom:1.0.3` en `pom.xml`
- [x] T002 [P] Añadir dependencias Tika `tika-core` + `tika-parser-pdf-module:3.3.2` en `pom.xml`
- [x] T003 [P] Añadir `sqlite-jdbc`, `hibernate-community-dialects`, Flyway en `pom.xml`
- [x] T004 Configurar `src/main/resources/application.properties` (datasource `jdbc:sqlite:oposiciones.db` WAL, `spring.ai.ollama.*` llama3.1:8b + nomic-embed-text)
- [x] T005 Crear `data/temario/.gitkeep` + entrada `gitignore` para PDFs y `oposiciones.db`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Persistencia + utilidades compartidas que bloquean todas las historias.

**⚠️ CRITICAL**: Ninguna historia empieza hasta completar esta fase.

- [x] T006 Crear migración `src/main/resources/db/migration/V1__temario.sql` (tablas temas, fragmentos UNIQUE(tema_id, orden), chunk_embedding)
- [x] T007 [P] Crear entidades JPA `Tema`, `Fragmento`, `ChunkEmbedding` en `src/main/java/com/oposiciones/temario/`
- [x] T008 [P] Crear repositorios Spring Data en `src/main/java/com/oposiciones/temario/` (TemaRepository con findByContentSha256, FragmentoRepository, EmbeddingRepository)
- [x] T009 Crear `Sha256` + `PdfTextExtractor` (Tika, orden + nº página) en `src/main/java/com/oposiciones/temario/`
- [x] T010 Crear `TextChunker` (~2000 chars/overlap 200, detección multi-tema ">1 patrón TEMA") en `src/main/java/com/oposiciones/temario/`
- [x] T011 Crear `EmbeddingService` (Spring AI Ollama nomic-embed-text, normalización, BLOB LE, coseno brute-force top-K) en `src/main/java/com/oposiciones/temario/`

**Checkpoint**: Foundation ready — `mvn test` compila, Flyway migra en vacío, Ollama reachable tras `ollama list`.

---

## Phase 3: User Story 1 - Subir temario y ver temas (Priority: P1) 🎯 MVP

**Goal**: Cargar 1-N PDFs y verlos listados con título/páginas/estado.

**Independent Test**: Subir 2 PDFs → 2 temas LISTO con texto recuperable (quickstart pasos 2-3).

### Tests for User Story 1

> NOTE: Escribir FIRST, verificar FAIL antes de implementar.

- [x] T012 [P] [US1] Test ingesta válida e ingesta corrupta en `src/test/java/com/oposiciones/temario/TemarioServiceTest.java`
- [x] T013 [P] [US1] Test contrato `POST /api/v1/temas` + `GET /api/v1/temas` en `src/test/java/com/oposiciones/temario/TemarioControllerTest.java` (MockMvc)

### Implementation for User Story 1

- [x] T014 [US1] Implementar `TemarioService.ingestar(recursos)` con estados PROCESANDO→LISTO/ERROR y progreso por lotes en `src/main/java/com/oposiciones/temario/TemarioService.java` (depende de T007-T011)
- [x] T015 [US1] Implementar `TemarioController` `POST /api/v1/temas` multipart + `GET /api/v1/temas` + `GET /api/v1/temas/{id}` con 201/422 según `contracts/temario-api.yaml` en `src/main/java/com/oposiciones/temario/TemarioController.java`
- [x] T016 [US1] Añadir PDFs de prueba en `src/test/resources/temario/` (válido + corrupto) y `data/temario/README.md`

**Checkpoint**: US1 funciona sola — quickstart pasos 2-3 en verde.

---

## Phase 4: User Story 2 - Contenido fiel y localizable (Priority: P2)

**Goal**: Fragmentos literales con cita tema+posición y búsqueda por palabra.

**Independent Test**: Frase literal de pág. 10 recuperada con cita correcta; borrados excluidos (quickstart paso 4).

### Tests for User Story 2

- [x] T017 [P] [US2] Test fidelidad literal + cita (orden/página) en `src/test/java/com/oposiciones/temario/FragmentoStoreTest.java`
- [x] T018 [P] [US2] Test contrato `GET /api/v1/temas/{id}/fragmentos` + `GET /api/v1/buscar?q=` en `src/test/java/com/oposiciones/temario/BusquedaControllerTest.java` (MockMvc)

### Implementation for User Story 2

- [x] T019 [US2] Implementar `GET /api/v1/temas/{id}/fragmentos` paginado (orden, página, texto) en `src/main/java/com/oposiciones/temario/TemarioController.java`
- [x] T020 [US2] Implementar `GET /api/v1/buscar` textual + semántico top-K con cita en `src/main/java/com/oposiciones/temario/TemarioController.java` (usa T011)

**Checkpoint**: US1 + US2 funcionan; quickstart paso 4 en verde.

---

## Phase 5: User Story 3 - Actualizar y borrar (Priority: P3)

**Goal**: Reemplazar PDF (conserva id+título), renombrar, borrar en cascada; dedup SHA-256 y rechazo multi-tema.

**Independent Test**: Reemplazo conserva título; duplicado 422; multi-tema 422; borrado desaparece de listado y búsqueda (quickstart pasos 5-8).

### Tests for User Story 3

- [x] T021 [P] [US3] Test reemplazo conserva título + dedup SHA-256 + rechazo multi-tema en `src/test/java/com/oposiciones/temario/TemarioServiceTest.java`
- [x] T022 [P] [US3] Test contrato `PUT /temas/{id}/pdf` + `PATCH /temas/{id}/titulo` + `DELETE /temas/{id}` en `src/test/java/com/oposiciones/temario/TemarioControllerTest.java` (MockMvc)

### Implementation for User Story 3

- [x] T023 [US3] Implementar reemplazo (nuevo sha, borra fragmentos viejos, conserva título; 422 si idéntico) en `src/main/java/com/oposiciones/temario/TemarioService.java`
- [x] T024 [US3] Implementar `PUT /temas/{id}/pdf` + `PATCH /temas/{id}/titulo` + `DELETE /temas/{id}` cascada en `src/main/java/com/oposiciones/temario/TemarioController.java`

**Checkpoint**: Las 3 historias funcionan independientes; quickstart pasos 5-8 en verde.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cierre offline + validación completa.

- [x] T025 [P] Verificar offline + reinicio (SC-004) y documentar en `specs/001-ingesta-pdf-temario/quickstart.md`
- [x] T026 Ejecutar validación completa `quickstart.md` pasos 1-10 y `./mvnw test` en verde
- [x] T027 Limpieza: WAL/checkpoint, `README.md` con arranque local, sin PDFs reales commiteados

---

## Dependencies & Execution Order

- Setup (T001-T005) → Foundational (T006-T011, BLOCKS) → US1 (T012-T016, MVP) → US2 (T017-T020) → US3 (T021-T024) → Polish (T025-T027).
- US2/US3 pueden solaparse tras Foundational solo si hay capacidad; por defecto secuencial P1→P2→P3.
- Dentro de cada historia: tests FAIL primero → entidades → servicio → endpoints.

### Parallel Opportunities

- [P] T002, T003, T007, T008, T009+T010 (ficheros distintos).
- Tests de cada historia (T012+T013, T017+T018, T021+T022) en paralelo entre sí.
- T025 doc en paralelo con T026 solo si distinta máquina; T026 bloquea T027.

## Implementation Strategy

**MVP**: Fases 1+2+3 (T001-T016) → STOP, validar quickstart 2-3, demo carga de temario.
**Incremental**: +US2 (búsqueda fiel) → +US3 (mantenimiento) → polish offline.
