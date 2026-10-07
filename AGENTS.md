# AGENTS.md — oposiciones-ia

Proyecto: estudio de oposiciones TAI 100% local (Java 21 + Spring Boot 3.3 + Ollama + SQLite + voz `say`).

## Comandos

- Tests: `mvn -B test` (SQLite en `target/`, embeddings y TTS mockeados; sin red salvo Maven Central)
- Arranque: `mvn -B spring-boot:run` (requiere Ollama con `nomic-embed-text`; `say` solo en macOS)
- Importante: `spring-boot:run` NO recopia recursos → tras cambiar `src/main/resources`, ejecutar `mvn -B -q process-resources` antes
- Cerrar app: `pkill -f ExamprepApplication` (el fork JVM sobrevive a `pkill spring-boot:run`; NO dejar 2 JVMs: el puerto 8080 lo retiene la más vieja)

## Gates obligatorios (constitution v1.3.0)

- **Commit gate: PROHIBIDO commitear/pushear con tests en rojo. Todo commit exige `mvn -B test` 100% verde justo antes.**
- **English code: identificadores + comentarios + JavaDoc en inglés; mensajes visibles al usuario en español.**
- Local-first: Ollama `localhost:11434`, SQLite fichero, nada de nube.
- Nunca commitear PDFs del temario ni `*.db` (ver `.gitignore`: `data/temario/*.pdf`, `data/audio/`, `oposiciones.db*`).
- Spec-driven: código solo desde `specs/NNN-*/tasks.md`; marcar tareas `[x]` al completar.

## Notas técnicas

- SQLite + Flyway: `ddl-auto=none` (Flyway dueño; `INTEGER PRIMARY KEY AUTOINCREMENT` choca con validación BIGINT de Hibernate).
- SQLite concurrente: URL con `journal_mode=WAL&busy_timeout=30000`; pool async máx 4 hilos.
- Voz `say`: nombre EXACTO con acento (`Mónica`); `Monica` sin acento cae en voz inglesa sin error.
- WAV de `say` trae chunk FLLR: duraciones solo con parser que recorra chunks (ver `SayTtsService.WavUtil`).
