# Data Model: Estudio Tema por Voz (002)

**Reutiliza**: `Tema`, `Fragmento` de 001. **Nuevas tablas** en `V2__estudio_voz.sql`.

## Entidad AudioFragmento (técnica, 1:1 con Fragmento)

| Campo | Tipo | Reglas |
|-------|------|--------|
| fragmento_id | INTEGER PK FK → fragmentos(id) ON DELETE CASCADE | Uno por fragmento narrado |
| duracion_seg | REAL NOT NULL | > 0, duración del WAV |
| generado_en | TIMESTAMP NOT NULL | Cuándo se sintetizó |

Fichero asociado: `data/audio/{temaId}/{fragmentoId}.wav` (WAV 22050Hz mono 16-bit). Ausencia de fila o de fichero ⇒ generar bajo demanda al pedir el audio (fallback).

## Entidad ProgresoEscucha (1:1 con Tema)

| Campo | Tipo | Reglas |
|-------|------|--------|
| tema_id | INTEGER PK FK → temas(id) ON DELETE CASCADE | Un progreso por tema |
| fragmento_id | INTEGER NOT NULL | Fragmento donde se pausó; debe pertenecer al tema |
| offset_seg | REAL NOT NULL | ≥ 0 y ≤ duración del fragmento; segundo exacto |
| actualizado_en | TIMESTAMP NOT NULL | Última pausa/cierre |

**Transiciones**: se crea/actualiza en pausa o cierre; se borra al terminar la narración (fin del último fragmento), al reemplazar el PDF (contenido nuevo) y en cascada al borrar el tema.

## Relaciones

- Tema 1—1 ProgresoEscucha (cascada).
- Fragmento 1—1 AudioFragmento (cascada vía fragmentos).
- `fragmento_id` del progreso siempre referencia un fragmento del mismo tema (validado en servicio).

## Volumetría v1

~1200 fragmentos × ~200KB WAV ≈ 240MB por 20 temas en `data/audio/` (gitignored, borrable y regenerable).
