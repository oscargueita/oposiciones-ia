# Data Model: Chuletas y Mapas (005)

**Migración** `V6__chuletas_mapas.sql`.

## Entidad Chuleta (1:1 con Tema)

| Campo | Tipo | Reglas |
|-------|------|--------|
| tema_id | INTEGER PK FK → temas(id) ON DELETE CASCADE | Una chuleta por tema |
| markdown | TEXT NOT NULL | No vacío, con citas `(tema, pág. N)` |
| creado_en | TIMESTAMP NOT NULL | Generación (para saber si es anterior a un reemplazo) |

## Entidad Mapa (1:1 con Tema)

| Campo | Tipo | Reglas |
|-------|------|--------|
| tema_id | INTEGER PK FK → temas(id) ON DELETE CASCADE | Un mapa por tema |
| mermaid | TEXT NOT NULL | Bloque `mindmap`, 8-15 nodos, sintaxis verificada |
| creado_en | TIMESTAMP NOT NULL | Generación |

**Invalidación**: borrado en cascada por FK + borrado explícito al reemplazar (vía `TopicContentDeletedEvent`, igual que audios/progreso).
