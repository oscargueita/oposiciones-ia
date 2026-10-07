# Feature Specification: Frontend React

**Feature Branch**: `006-frontend-react`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "App web local con React: ver temas, subir PDFs, reproductor con continuar, repaso, tests, chuletas y mapas"

## Clarifications

### Session 2026-10-07

- Q: ¿La app debe organizarse en vistas separadas por función o en una sola página con secciones? → A: Opción A — vistas separadas: Temas, Repaso, Tests, Chuleta/Mapa.
- Q: ¿El reproductor debe avanzar solo al siguiente fragmento o esperar que pulses cada uno? → A: Opción A — avance automático al siguiente fragmento.
- Q: ¿Las preguntas del test se muestran de una en una o todas seguidas? → A: Opción A — una pregunta por pantalla con su feedback.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Estudiar un tema escuchándolo (Priority: P1)

El opositor ve la lista de temas, sube PDFs nuevos, elige uno y lo escucha con play/pausa; al volver continúa donde lo dejó.

**Why this priority**: Es el uso diario central y cubre ingesta + voz desde la interfaz.

**Independent Test**: Subir un PDF, verlo en lista, reproducir 3 fragmentos, pausar, recargar y continuar en el punto.

**Acceptance Scenarios**:

1. **Given** la app abierta, **When** el usuario sube un PDF, **Then** aparece en la lista con su estado.
2. **Given** un tema con audios, **When** el usuario reproduce y pausa, **Then** al volver continúa en el segundo exacto.

---

### User Story 2 - Repasar por palabra clave (Priority: P2)

El opositor escribe una palabra, ve los 5 candidatos con cita y escucha el elegido.

**Why this priority**: Repaso rápido sin tocar la terminal.

**Independent Test**: Buscar "recurso de alzada", ver 5 citas y escuchar el primero.

**Acceptance Scenarios**:

1. **Given** una palabra con resultados, **When** el usuario la busca, **Then** ve candidatos ordenados con cita y audio cada uno.
2. **Given** una palabra sin resultados, **When** la busca, **Then** ve aviso claro sin audio.

---

### User Story 3 - Hacer tests con nota (Priority: P3)

El opositor genera un test por tema y dificultad, responde con feedback inmediato y ve nota e historial.

**Why this priority**: Autoevaluación completa desde la interfaz.

**Independent Test**: Generar 5 preguntas, responderlas, ver nota  y el intento en el historial.

**Acceptance Scenarios**:

1. **Given** un tema, **When** genera un test, **Then** responde con feedback por pregunta y nota final.
2. **Given** intentos anteriores, **When** abre el historial, **Then** ve notas y fechas.

---

### User Story 4 - Ver chuleta y mapa (Priority: P4)

El opositor abre la chuleta renderizada y el mapa como diagrama de un tema.

**Why this priority**: Repaso visual final antes del examen.

**Independent Test**: Abrir chuleta con formato y mapa dibujado de un tema.

**Acceptance Scenarios**:

1. **Given** un tema, **When** abre su chuleta, **Then** ve el resumen con formato y citas.
2. **Given** un tema, **When** abre su mapa, **Then** ve el diagrama de conceptos dibujado.

---

### Edge Cases

- API caída o sin temas: mensaje claro con acción (arrancar backend / subir PDF).
- Audio aún generándose: el fragmento muestra espera y reintenta solo.
- Tema borrado mientras se estudia: aviso y vuelta al listado.
- Sin conexión a internet: todo funciona (backend local).

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST mostrar lista de temas con estado y permitir subir PDFs.
- **FR-002**: System MUST reproducir audios por fragmento en orden con avance automático, pausa y reanudación exacta.
- **FR-003**: System MUST persistir el punto de escucha al pausar/cerrar y ofrecer continuar.
- **FR-004**: System MUST buscar por palabra con candidatos, citas y audio.
- **FR-005**: System MUST generar tests, responder una pregunta por pantalla con feedback inmediato y mostrar nota e historial.
- **FR-006**: System MUST renderizar chuleta con formato y mapa como diagrama.
- **FR-007**: System MUST funcionar contra el backend local sin internet.
- **FR-008**: Users MUST be able to usar toda la app sin terminal.

### Key Entities

- Ninguna nueva: reutiliza Tema, Fragmento, Progreso, Test, Chuleta y Mapa vía la API existente.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Usuarios suben un PDF y escuchan su primer audio en menos de 5 minutos sin ayuda.
- **SC-002**: El 90% continúa una escucha tras recargar sin perder el punto.
- **SC-003**: Todas las funciones del backend (temas, voz, repaso, tests, chuletas, mapas) son usables sin terminal.
- **SC-004**: La app carga en local en menos de 3 segundos.

## Assumptions

- Un único usuario local; sin login ni multiusuario en v1.
- El backend sirve el frontend compilado (misma URL, sin CORS en producción); en desarrollo con proxy.
- Formatos estándar web para markdown y diagramas (detalle al plan).
