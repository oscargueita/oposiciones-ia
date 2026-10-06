<!-- Sync Impact Report
Version change: 1.0.0 -> 1.1.0 (commit gate: tests en verde obligatorios)
Modified principles: none
Added sections: Commit gate en Development Workflow y Governance
Removed sections: none
TODOs: none
-->
# Oposiciones IA Constitution

## Core Principles

### I. Local-First (NON-NEGOTIABLE)
Todo el sistema funciona 100% en local, sin dependencia de nube. Temario, embeddings, inferencia y audio nunca salen de la máquina del opositor. Solo se permite red para descargar modelos o dependencias Maven. Rationale: privacidad del temario y estudio offline.

### II. Stack Java 21 + Spring Boot 3
Backend en Java 21 + Spring Boot 3, gestionado desde IntelliJ, compilable con Maven/Gradle. Todo módulo nuevo expone servicio Spring testeable, sin lógica en controladores. Rationale: mantenibilidad y tooling del proyecto.

### III. RAG con Ollama local
Chat con `llama3.1:8b` y embeddings con `nomic-embed-text` vía Ollama (`http://localhost:11434`). Ingesta de PDFs con Apache Tika, chunk ~800 tokens con overlap, metadatos tema/página. Ningún chunk se inventa: toda respuesta de estudio/repaso cita tema + fragmento. Rationale: fidelidad al temario oficial.

### IV. Persistencia local SQLite/H2
Metadata (temas, chunks, tests, chuletas) en SQLite/H2 local en fichero versionable fuera de git. Vectores en store local persistente (SQLite-vec/Chroma embebido o fichero). Migraciones con Flyway/Liquibase. Rationale: cero infraestructura externa.

### V. TTS desacoplado
Voz tras interfaz `TtsService.textToSpeech(text)->audio`. Implementación v1: Piper local o `say` macOS. Ningún caso de uso (estudiar/repasar) llama al motor TTS directamente. Rationale: cambiar de voz sin tocar dominio.

### VI. Test-First (NON-NEGOTIABLE)
TDD: test escrito → aprobado → falla → implementar. Cada feature incluye unit + integración (ingesta PDF real, RAG contra Ollama Testcontainers/mock, endpoints REST). Cobertura mínima en servicios de dominio. Rationale: fiabilidad del material de estudio.

### VII. Simplicidad y Spec-Driven
YAGNI: empezar monolito modular, sin microservicios. Ningún código sin spec aprobada en `.specify/specs/`. Complejidad justificada en plan. Rationale: evitar sobrediseño agéntico.

## Technology Stack Constraints

- Java 21 LTS, Spring Boot 3.x, Spring AI + Ollama, Apache Tika, SQLite/H2 + Flyway.
- Modelos fijados: `llama3.1:8b`, `nomic-embed-text`. Cambio de modelo requiere enmienda constitution + reindexado documentado.
- PDFs como fuente de verdad en `data/temario/` (gitignored). Nunca commitear temario con copyright.
- API REST + CLI; mapas conceptuales en Mermaid/Markdown; chuletas en Markdown 1 página.

## Development Workflow

Flujo obligatorio: `/speckit-specify` → `/speckit-plan` → `/speckit-tasks` → `/speckit-implement` → `/speckit-converge`. Revisión humana entre fases. Commits en español con formato `docs|feat|fix: ...`. **Commit gate (NON-NEGOTIABLE): prohibido commitear o pushear con tests en rojo; todo commit exige `mvn -B test` con 100% en verde justo antes.** `constitution.md` manda sobre cualquier guía en `AGENTS.md`.

## Governance

Esta constitution prevalece sobre toda práctica. Enmiendas con Sync Impact Report, versionado semántico (MAJOR ruptura, MINOR principio nuevo, PATCH clarificación) y revisión en PR. Cada PR verifica: local-first, tests en verde, RAG cita fuente, TTS desacoplado. Ningún commit ni push con `mvn -B test` en rojo, sin excepciones.

**Version**: 1.1.0 | **Ratified**: 2026-10-06 | **Last Amended**: 2026-10-07
