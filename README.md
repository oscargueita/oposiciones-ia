# Oposiciones IA — estudio de oposiciones 100% local

Estudia el temario de tu oposición en local: ingesta de PDFs, escucha por voz,
repaso por palabra clave, tests auto-generados. Sin nube: LLM, embeddings,
voz y base de datos corren en tu máquina.

## Requisitos

- JDK 21 · Ollama · `say` (macOS) o Piper (Linux) — todo lo instala/verifica `scripts/setup.sh`
- 8GB libres (modelos ~5GB + audios ~2GB con temario completo en M4A)

## Puesta en marcha (máquina nueva)

```bash
git clone https://github.com/oscargueita/oposiciones-ia.git
cd oposiciones-ia
./scripts/setup.sh            # instala/verifica Java, Ollama, modelos, voz y compila
./mvnw spring-boot:run        # API en http://localhost:8080
```

Linux o sin `say`: `./scripts/setup.sh --with-piper` y `app.tts.motor=piper` en
`src/main/resources/application.properties`.

## Cargar temario

```bash
curl -F "files=@data/temario/tu-tema.pdf" http://localhost:8080/api/v1/temas
```

- 1 PDF = 1 tema (los multi-tema se rechazan con aviso).
- La DB (`oposiciones.db`, gitignored) se migra sola con Flyway; los audios se generan en fondo.
- Material oficial de ejemplo: código BOE TAI (gratis) — ver `data/temario/README.md`.

## Endpoints principales

| Método | Ruta | Qué hace |
|---|---|---|
| POST | `/api/v1/temas` (multipart `files`) | Subir PDFs |
| GET | `/api/v1/temas` | Listar temas y estado |
| GET | `/api/v1/temas/{id}/narracion` | Playlist con audios y duraciones |
| GET | `/api/v1/fragmentos/{fid}/audio` | Audio del fragmento |
| GET/PUT/DELETE | `/api/v1/temas/{id}/progreso` | Punto de escucha exacto |
| GET | `/api/v1/repasar?q=&topicId?&topK=` | Top-5 por palabra clave con cita |
| POST | `/api/v1/temas/{id}/tests?n=&difficulty=` | Generar test (EASY/MEDIUM/HARD) |
| POST | `/api/v1/tests/{id}/responder` + `/finalizar` | Feedback inmediato + nota |
| GET | `/api/v1/temas/{id}/tests` | Historial con notas |

## Desarrollo (spec-driven)

Flujo por feature: `/speckit-specify` → `/speckit-clarify` → `/speckit-plan` →
`/speckit-tasks` → `/speckit-implement`. Specs en `specs/NNN-*/`.

**Commit gate (constitution v1.1.0): prohibido commitear/pushear con tests en rojo —
todo commit exige `mvn -B test` 100% verde justo antes.**

## Estructura

```
src/main/java/com/oposiciones/
  temario/   # 001 ingesta PDF (Tika + embeddings Ollama + SQLite)
  voz/       # 002 TTS desacoplado (say/Piper) + progreso
  repaso/    # 003 búsqueda híbrida literal+semántica
  examen/    # 004 tests con LLM local + corrección
specs/       # especificaciones por feature
scripts/setup.sh  # instalación automática
```
