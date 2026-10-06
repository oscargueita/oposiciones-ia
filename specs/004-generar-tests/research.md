# Research: Generar Tests (004)

**Fecha**: 2026-10-07 | **Base**: 001 (fragmentos), Ollama `llama3.1:8b` vía Spring AI.

## Decision 1: JSON estructurado con validador

- **Decision**: Prompt que exige 1 pregunta JSON por fragmento (`{enunciado, opciones[4], correcta(0-3), explicacion}`) + `BeanOutputConverter<PreguntaJson>` de Spring AI para parseo tipado. Validador posterior: 4 opciones distintas no vacías, `correcta` en rango, explicación no vacía, cita = fragmento usado. Fallo ⇒ reintento con otro fragmento (máx 3 por pregunta).
- **Rationale**: El modelo pequeño a veces devuelve texto libre o índices inválidos; el conversor + validador convierten salidas malas en reintentos en vez de preguntas rotas (SC-001 95% válidas).
- **Alternatives considered**: Regex casero sobre texto libre (frágil); `response_format=json_object` de Ollama (ayuda pero no garantiza esquema; se combina si el modelo lo soporta).

## Decision 2: Dificultad en el prompt

- **Decision**: Tres plantillas: fácil (pregunta literal "según el artículo X, ¿cuál es..."), medio (parafraseo + distractores del mismo fragmento), difícil (caso aplicado que combina 2 fragmentos). La dificultad se guarda en el test.
- **Rationale**: Implementa aclaración B sin cambiar arquitectura.
- **Alternatives considered**: Dificultad por longitud de fragmento (impreciso, rechazado).

## Decision 3: Muestreo de fragmentos y snapshot

- **Decision**: Muestreo aleatorio sin reemplazo de fragmentos del tema (semilla = test id); 1 pregunta por fragmento; si N > fragmentos ⇒ aviso y N = fragmentos (FR-007). Snapshot: enunciado+opciones+correcta+explicación+cita copiados en `pregunta` (el test sobrevive a reemplazos del tema).
- **Rationale**: Garantiza variedad (intentos siempre nuevos por muestreo distinto) y corrección histórica.
- **Alternatives considered**: Re-muestrear hasta N distinto (complejo, el muestreo aleatorio ya varía).

## Decision 4: Corrección inmediata + nota

- **Decision**: `POST /tests/{id}/responder {preguntaId, opcion}` ⇒ feedback inmediato (acierto + explicación + cita); `POST /tests/{id}/finalizar` ⇒ nota = 10·aciertos/total + repaso completo. Sin responder cuenta como fallo al finalizar.
- **Rationale**: Aclaración B (feedback inmediato + nota final).
- **Alternatives considered**: Corrección solo final (rechazado en clarify).
