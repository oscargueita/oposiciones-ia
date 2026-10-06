# Quickstart: Generar Tests (004)

**Prerrequisitos**: App con temario TAI + Ollama `llama3.1:8b`.

## Validación end-to-end

1. Generar: `curl -X POST "http://localhost:8080/api/v1/temas/2/tests?n=3&dificultad=MEDIO"` → 201 con 3 preguntas de 4 opciones.
2. Verificar cita: cada pregunta trae `citaFragmentoId` + página existentes.
3. Responder: `curl -X POST .../tests/{id}/responder -d '{"preguntaId":P,"opcion":0}'` → acierto + explicación.
4. Finalizar: `curl -X POST .../tests/{id}/finalizar` → nota sobre 10 + repaso.
5. Historial: `GET .../temas/2/tests` → el intento con su nota.
6. Generar otro: preguntas distintas al anterior (muestreo).
7. N excesiva: `?n=500` → aviso + máximo disponible.
8. Offline: repetir 1-5 sin red → idéntico (Ollama local).
9. Tests: `mvn -B test` (chat mockeado con JSON fijo + 1 test opcional contra Ollama real).

**Esperado**: SC-001–SC-004 de `spec.md`.
