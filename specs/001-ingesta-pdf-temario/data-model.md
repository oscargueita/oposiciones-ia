# Data Model: Ingesta PDF Temario (001)

**Fuente**: `spec.md` entidades Tema + Fragmento, aclaraciones 2026-10-06.

## Entidad Tema

| Campo | Tipo | Reglas |
|-------|------|--------|
| id | INTEGER PK autoincrement | Identidad interna |
| titulo | TEXT NOT NULL | Inicial = nombre fichero sin extensión; editable; se conserva en reemplazo |
| origen_nombre | TEXT NOT NULL | Nombre original del fichero subido |
| content_sha256 | CHAR(64) NOT NULL UNIQUE | SHA-256 de los bytes del PDF; anti-duplicado aunque cambie el nombre |
| num_paginas | INTEGER NOT NULL | > 0, extraído por Tika |
| estado | TEXT NOT NULL | `PROCESANDO` → `LISTO` / `ERROR`; nunca visible como LISTO a medias |
| mensaje_error | TEXT NULL | Motivo accionable si estado ERROR (corrupto, sin texto, multi-tema, duplicado) |
| creado_en | TIMESTAMP NOT NULL | Fecha de carga |

**Transiciones**: `PROCESANDO → LISTO | ERROR`. Reemplazo: mismo id, nuevo sha + páginas, título intacto, fragmentos viejos borrados. Borrado: tema + fragmentos + embeddings en cascada.

## Entidad Fragmento (Chunk)

| Campo | Tipo | Reglas |
|-------|------|--------|
| id | INTEGER PK autoincrement | Identidad interna |
| tema_id | INTEGER NOT NULL FK → temas(id) ON DELETE CASCADE | Pertenencia obligatoria |
| orden | INTEGER NOT NULL | 0..N secuencial dentro del tema; UNIQUE(tema_id, orden) |
| pagina | INTEGER NOT NULL | Página origen ≥ 1 |
| texto | TEXT NOT NULL | Texto literal del PDF, no vacío |

## Entidad Embedding (técnica, 1:1 con Fragmento)

| Campo | Tipo | Reglas |
|-------|------|--------|
| chunk_id | INTEGER PK FK → fragmentos(id) ON DELETE CASCADE | Uno por fragmento |
| embedding | BLOB NOT NULL | 3072 bytes = 768 float32 LE normalizados |

## Relaciones

- Tema 1—N Fragmento (cascada en borrado/reemplazo).
- Fragmento 1—1 Embedding (se crea junto al chunk en ingesta).

## Volumetría v1

20 temas × ~30 páginas × ~2 chunks/página ≈ 1200 fragmentos; embeddings ≈ 3.7MB. Sin límite de tamaño de PDF (decisión A): PDFs grandes generan más chunks por lotes sin bloquear el listado.

## Validaciones derivadas de FRs

- FR-005: rechazar sin texto extraíble (< 50 caracteres útiles), corruptos (Tika lanza excepción), multi-tema (>1 patrón "TEMA \d+" distinto).
- FR-006: reemplazo conserva id + título; valida que el nuevo sha ≠ sha actual (si igual → aviso "ya cargado").
- FR-009: borrado elimina fragmentos + embeddings; búsqueda posterior no devuelve nada del tema.
