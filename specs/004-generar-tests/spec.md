# Feature Specification: Generar Tests

**Feature Branch**: `004-generar-tests`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Crear tests tipo examen desde el temario (preguntas con opciones, corrección y nota)"

## Clarifications

### Session 2026-10-07

- Q: ¿La corrección debe mostrarse solo al final del test o dar feedback inmediato en cada pregunta? → A: Opción B — feedback inmediato tras cada respuesta.
- Q: ¿Las preguntas deben tener un único nivel o elegir dificultad al generar el test? → A: Opción B — fácil, medio y difícil a elegir.
- Q: ¿Se puede repetir el mismo test para mejorar nota o cada intento genera preguntas nuevas? → A: Opción B — cada intento genera preguntas nuevas.
- Q: ¿El test puede mezclar varios temas o todos? → A: Mezcla a elegir (lista de temas o todos), muestreo equilibrado entre temas.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generar un test de un tema (Priority: P1)

El opositor elige un tema y pide un test de 10 preguntas tipo examen (4 opciones, 1 correcta), generadas desde el texto oficial y cada una con cita del fragmento origen.

**Why this priority**: Es el uso central: autoevaluarse con preguntas fieles al temario.

**Independent Test**: Generar 10 preguntas de la Constitución y comprobar que cada una cita su fragmento y tiene 4 opciones con 1 correcta.

**Acceptance Scenarios**:

1. **Given** un tema listo con contenido, **When** el usuario genera un test de 10, **Then** recibe 10 preguntas con 4 opciones, respuesta correcta y cita.
2. **Given** un tema vacío o en error, **When** el usuario pide un test, **Then** recibe un mensaje claro y no se genera nada.

---

### User Story 2 - Responder con feedback inmediato y ver nota (Priority: P2)

El opositor responde cada pregunta y al momento ve si acertó con la explicación y cita; al terminar recibe la nota global sobre 10.

**Why this priority**: El feedback inmediato fija el concepto en el momento; la nota final mide el conjunto.

**Independent Test**: Responder 10 (7 bien, 3 mal) viendo feedback por pregunta y comprobar nota 7.0 al final.

**Acceptance Scenarios**:

1. **Given** una pregunta respondida, **When** el usuario envía su respuesta, **Then** ve al momento si acertó con explicación y cita.
2. **Given** el test terminado, **When** el usuario lo finaliza, **Then** recibe nota, aciertos/fallos y repaso de todas las preguntas.

---

### User Story 3 - Historial por tema (Priority: P3)

El opositor consulta sus notas anteriores por tema para ver progreso.

**Why this priority**: Medir evolución entre sesiones mantiene la motivación y dirige el repaso.

**Independent Test**: Hacer 2 tests del mismo tema y ver ambas notas ordenadas por fecha.

**Acceptance Scenarios**:

1. **Given** tests corregidos de un tema, **When** el usuario abre el historial, **Then** ve fecha, nota y aciertos de cada intento.
2. **Given** un tema sin intentos, **When** abre el historial, **Then** ve aviso de sin intentos.

---

### Edge Cases

- Pedir más preguntas que fragmentos disponibles: se avisa del máximo y se genera ese máximo.
- Pregunta generada sin respuesta válida en el fragmento: se descarta y se genera otra (reintento acotado).
- Opciones duplicadas o sin correcta única: la pregunta se descarta.
- Tema reemplazado tras generar el test: el test guarda copia de preguntas/respuestas y sigue corregible.
- Caracteres especiales del texto legal se conservan legibles en enunciados.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST generar N preguntas (1-50, 10 por defecto) de un tema listo, cada una con 4 opciones y 1 correcta.
- **FR-002**: System MUST citar el fragmento origen en cada pregunta y explicación.
- **FR-003**: System MUST validar cada pregunta (4 opciones distintas, 1 correcta, respuesta apoyada en el fragmento) y descartar las inválidas con reintento acotado.
- **FR-004**: System MUST responder a cada pregunta con acierto/fallo inmediato más explicación y cita, y calcular la nota sobre 10 al finalizar.
- **FR-005**: System MUST mostrar al final el repaso completo con la correcta, explicación y cita por pregunta.
- **FR-006**: System MUST persistir tests generados y resultados con fecha para historial por tema; cada intento genera preguntas nuevas (sin repetición del mismo test).
- **FR-007**: System MUST avisar y limitar cuando N supera la materia disponible.
- **FR-008**: System MUST rechazar generar de temas vacíos, en proceso o en error.
- **FR-009**: System MUST funcionar sin conexión a internet (generación local).
- **FR-010**: System MUST permitir elegir dificultad (fácil, medio, difícil) al generar el test y guardarla con él.
- **FR-011**: System MUST permitir generar un test mixto de varios temas elegidos o de todos, muestreando equilibrado entre temas; cada pregunta cita su tema.

### Key Entities

- **TestGenerado**: intento de test; atributos: tema, dificultad (fácil/medio/difícil), fecha, nº preguntas, estado (pendiente/corregido).
- **Pregunta**: enunciado, 4 opciones, índice correcta, explicación, cita (tema+fragmento+página).
- **Resultado**: test, respuestas dadas, aciertos, nota sobre 10, fecha.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 95% de preguntas generadas son válidas (4 opciones, 1 correcta, cita real) en muestra de 40.
- **SC-002**: Usuarios generan y corrigen un test de 10 en menos de 5 minutos.
- **SC-003**: El 100% de correcciones calcula bien la nota verificada contra corrección manual en 5 tests.
- **SC-004**: Generación y corrección funcionan con la red desconectada.

## Assumptions

- 4 opciones con 1 correcta (formato examen TAI); verdadero/falso y desarrollo diferidos.
- Nota sobre 10 sin negativos (negativos diferidos a v2).
- Generación con el modelo local de chat sobre fragmentos del tema (detalle al plan).
- Test mixto multi-tema diferido a v2.
