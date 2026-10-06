# Implementation Plan: Ingesta PDF Temario

**Branch**: `001-ingesta-pdf-temario` | **Date**: 2026-10-06 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-ingesta-pdf-temario/spec.md` + stack del usuario: Spring Boot 3 + Tika + Ollama llama3.1:8b + nomic-embed-text + SQLite local + REST + Flyway.

## Summary

Ingesta local de PDFs del temario (1 PDF = 1 tema): subida multipart, extracción con Tika 3.3.2, chunking ~2000 chars/overlap 200, embeddings `nomic-embed-text` vía Spring AI 1.0.3, persistencia SQLite (`temas`, `fragmentos`, `chunk_embedding` BLOB) con Flyway, búsqueda brute-force por coseno y API REST de gestión. Sin límite de tamaño (lotes + progreso), dedup por SHA-256, título editable conservado en reemplazos.

## Technical Context

**Language/Version**: Java 21 LTS (Temurin verificado) + Maven.

**Primary Dependencies**: Spring Boot 3.3, `spring-ai-bom:1.0.3` + `spring-ai-ollama-spring-boot-starter`, Tika `tika-core` + `tika-parser-pdf-module:3.3.2`, `sqlite-jdbc:3.49+` + `hibernate-community-dialects`, Flyway.

**Storage**: SQLite fichero `oposiciones.db` (WAL) + tabla `chunk_embedding` BLOB 3072B/vector; `data/temario/` gitignored para PDFs.

**Testing**: JUnit5 + AssertJ + MockMvc + `@DataJpaTest`; embeddings stub en tests (sin Ollama), 1 test de integración con Ollama real opcional tras `ollama list`.

**Target Platform**: macOS/Linux escritorio local, offline tras instalación; Ollama en `http://localhost:11434`.

**Project Type**: Monolito modular (web-service REST sin frontend en v1).

**Performance Goals**: 1 PDF ~30 págs listo en < 2 min; 20 temas en < 30 min; búsqueda top-K < 1 s; ingesta por lotes sin bloquear listado (estado PROCESANDO).

**Constraints**: 100% local (constitution I); texto literal con cita tema+página (constitution III); `TtsService` fuera de alcance en esta feature; TDD obligatorio.

**Scale/Scope**: ~1200 fragmentos / ~4MB embeddings en v1; diseño válido hasta ~50k chunks (entonces migrar a `sqlite-vec`).

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] I Local-First: sin llamadas cloud; Ollama localhost + SQLite fichero. PASS.
- [x] II Java 21 + Boot 3: Maven, servicios Spring, sin lógica en controladores. PASS.
- [x] III RAG Ollama: modelos fijados `llama3.1:8b`/`nomic-embed-text`; citas tema+posición en fragmentos. PASS (chat no usado en esta feature, solo embeddings).
- [x] IV SQLite/H2 + Flyway: migraciones versionadas, `data/` fuera de git. PASS.
- [x] V TTS desacoplado: fuera de alcance 001, sin acoplamiento introducido. PASS.
- [x] VI Test-First: plan prevé unit + MockMvc + ingesta real antes de implementar. PASS.
- [x] VII Simplicidad: monolito, brute-force vs vector DB justificado en research Decision 4. PASS.

Post-Phase-1 re-check: sin violaciones; ninguna entrada en Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-ingesta-pdf-temario/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── temario-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
src/main/java/com/oposiciones/
├── OposicionesApplication.java
├── temario/
│   ├── Tema.java / Fragmento.java / ChunkEmbedding.java (entidades JPA)
│   ├── TemaRepository.java / FragmentoRepository.java / EmbeddingRepository.java
│   ├── PdfTextExtractor.java (Tika)
│   ├── TextChunker.java (2000/200 + detección multi-tema)
│   ├── Sha256.java (identidad)
│   ├── EmbeddingService.java (Spring AI Ollama + normalización + brute-force top-K)
│   ├── TemarioService.java (orquesta ingesta por lotes + estados)
│   └── TemarioController.java (REST según contracts/temario-api.yaml)
├── config/OllamaConfig.java
└── resources/db/migration/V1__temario.sql
src/test/... (unit + MockMvc + resources PDFs prueba)
```

**Structure Decision**: Proyecto Maven único bajo `src/` existente; paquete `temario` por feature (monolito modular). Tests espejo en `src/test`.

## Complexity Tracking

> Sin violaciones de constitution — tabla vacía intencionadamente.
