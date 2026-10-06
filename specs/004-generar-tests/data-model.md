# Data Model: Generar Tests (004)

**Migración** `V3__tests.sql`.

## Entidad TestGenerado

| Campo | Tipo | Reglas |
|-------|------|--------|
| id | INTEGER PK autoincrement | Identidad |
| tema_id | INTEGER NOT NULL FK → temas(id) ON DELETE CASCADE | Tema origen |
| dificultad | TEXT NOT NULL | `FACIL`, `MEDIO`, `DIFICIL` |
| num_preguntas | INTEGER NOT NULL | 1-50, pedidas y generadas (avisado si recortado) |
| estado | TEXT NOT NULL | `PENDIENTE` → `CORREGIDO` |
| creado_en | TIMESTAMP NOT NULL | Fecha del intento (historial) |

## Entidad Pregunta (snapshot)

| Campo | Tipo | Reglas |
|-------|------|--------|
| id | INTEGER PK autoincrement | Identidad |
| test_id | INTEGER NOT NULL FK → test_generado(id) ON DELETE CASCADE | Pertenencia |
| orden | INTEGER NOT NULL | 0..N-1; UNIQUE(test_id, orden) |
| enunciado | TEXT NOT NULL | No vacío |
| opciones | TEXT NOT NULL | JSON array de exactamente 4 strings distintas no vacías |
| correcta | INTEGER NOT NULL | 0-3 |
| explicacion | TEXT NOT NULL | No vacía |
| cita_tema_id / cita_fragmento_id / cita_pagina | INTEGER NOT NULL | Cita al fragmento origen |

## Entidad Respuesta (una por pregunta respondida)

| Campo | Tipo | Reglas |
|-------|------|--------|
| pregunta_id | INTEGER PK FK → pregunta(id) ON DELETE CASCADE | Una respuesta por pregunta (upsert si repite) |
| opcion | INTEGER NOT NULL | 0-3 |
| acierto | INTEGER NOT NULL | 0/1 calculado al responder |

Nota = 10.0 · sum(acierto) / num_preguntas (sin responder = fallo al finalizar).

## Relaciones

- Tema 1—N TestGenerado (cascada); TestGenerado 1—N Pregunta (cascada, snapshot); Pregunta 1—1 Respuesta.
