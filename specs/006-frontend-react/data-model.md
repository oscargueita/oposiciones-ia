# Data Model: Frontend React (006)

**Sin persistencia nueva.** Estado solo en cliente (React state + React Query o fetch):

## Vistas y estado

| Vista | Ruta | Estado local | API usada |
|-------|------|--------------|-----------|
| Temas | `/temas` | lista, subida en curso | `GET /temas`, `POST /temas` |
| Estudio | `/temas/:id` (dentro de Temas) | fragmento actual, offset, playlist | narración, audio, progreso |
| Repaso | `/repaso` | query, temaId, candidatos | `GET /repasar` |
| Tests | `/tests` | test activo, pregunta i/N, nota | tests, responder, finalizar, historial |
| Material | `/material` | tema elegido, pestaña chuleta/mapa | chuleta, mapa |

El punto de escucha vive en el backend (`progreso_escucha`); el cliente solo lo lee/escribe.
