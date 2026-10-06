# Data Model: Repaso por Palabra Clave (003)

**Sin persistencia nueva.** Entidad efímera en memoria:

## ResultadoRepaso (no persistido)

| Campo | Tipo | Reglas |
|-------|------|--------|
| fragmentoId | Long | Existe en `fragmentos` |
| temaId | Long | Tema padre (filtro opcional) |
| pagina | int | Cita de posición |
| texto | String | Literal para mostrar |
| score | double | 0.5·coseno + 0.5·bonus literal; corte < 0.35 ⇒ lista vacía |
| audioUrl | String | `/api/v1/fragmentos/{id}/audio` (002, genera si falta) |

Reutiliza `Fragmento`, `Tema` (001) y `AudioFragmento` (002). Sin migración.
