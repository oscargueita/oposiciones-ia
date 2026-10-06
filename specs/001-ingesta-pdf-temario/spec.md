# Feature Specification: Ingesta PDF Temario

**Feature Branch**: `001-ingesta-pdf-temario`

**Created**: 2026-10-06

**Status**: Draft

**Input**: User description: "Ingesta PDF temario en local con Tika + embeddings Ollama + SQLite"

## Clarifications

### Session 2026-10-06

- Q: ¿Cómo debe comportarse el sistema si un PDF contiene varios temas juntos en lugar de un tema por fichero? → A: Opción B — mantener 1 PDF = 1 tema en v1, rechazar multi-tema con mensaje de dividir fichero.
- Q: ¿Cómo debe detectar el sistema que un PDF ya fue cargado para evitar temas duplicados? → A: Opción A — hash SHA-256 del contenido del fichero.
- Q: ¿De dónde debe salir el título del tema que se muestra en el listado? → A: Opción B — nombre del fichero como inicial editable por el usuario.
- Q: ¿Debe haber un tamaño máximo de PDF por tema en v1? → A: Opción A — sin límite, procesar cualquier tamaño por lotes.
- Q: ¿Al reemplazar el PDF de un tema se debe conservar el título personalizado del usuario? → A: Opción A — conservar título personalizado al reemplazar PDF.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Subir temario y ver temas (Priority: P1)

El opositor selecciona uno o varios PDFs del temario desde su máquina y los carga en la app. Después ve la lista de temas disponibles con título y número de páginas/fragmentos.

**Why this priority**: Sin temario cargado ninguna otra feature (estudiar, repasar, tests, chuletas, mapas) funciona. Es el MVP fundacional.

**Independent Test**: Cargar 2 PDFs de prueba y comprobar que aparecen 2 temas listados con su contenido recuperable.

**Acceptance Scenarios**:

1. **Given** app vacía, **When** usuario carga un PDF válido de un tema, **Then** el tema aparece en el listado en menos de 2 minutos con su texto íntegro recuperable.
2. **Given** un PDF corrupto o protegido, **When** usuario intenta cargarlo, **Then** ve un error claro y ningún tema parcial queda en el listado.

---

### User Story 2 - Contenido fiel y localizable por partes (Priority: P2)

El opositor busca una parte del tema (por página o sección) y la app devuelve el texto exacto del temario, sin resúmenes inventados, indicando a qué tema y página pertenece.

**Why this priority**: Garantiza que estudiar/repasar por voz usará el texto oficial y no alucinaciones.

**Independent Test**: Cargar un tema de 20 páginas y recuperar literalmente un párrafo de la página 10 citando tema + página.

**Acceptance Scenarios**:

1. **Given** un tema cargado, **When** se pide un fragmento concreto, **Then** se devuelve el texto literal con referencia de tema y posición.
2. **Given** varios temas cargados, **When** se busca por palabra, **Then** solo se devuelven fragmentos de los temas cargados.

---

### User Story 3 - Actualizar y borrar temas (Priority: P3)

El opositor sustituye el PDF de un tema por una versión nueva o borra un tema que ya no entra en la oposición.

**Why this priority**: El temario cambia entre convocatorias; mantenerlo actualizado evita estudiar material obsoleto.

**Independent Test**: Sustituir el PDF de un tema y comprobar que el contenido nuevo reemplaza al viejo; borrar un tema y comprobar que desaparece del listado y de las búsquedas.

**Acceptance Scenarios**:

1. **Given** un tema existente, **When** se sube una nueva versión del PDF, **Then** el contenido queda reemplazado sin duplicados.
2. **Given** un tema existente, **When** se borra, **Then** ya no aparece ni es recuperable en búsquedas.

---

### Edge Cases

- PDF escaneado solo-imagen sin texto seleccionable: se rechaza con mensaje "sin texto extraíble".
- PDF multi-tema detectado: se rechaza con mensaje "divide el fichero, 1 PDF = 1 tema en v1".
- PDF de 500+ páginas o gran tamaño (sin límite en v1): se procesa por lotes con progreso visible, sin bloquear el listado.
- Mismo PDF subido dos veces (mismo hash SHA-256 aunque cambie el nombre): se detecta duplicado y no crea tema repetido, se avisa al usuario.
- Corte de proceso a mitad de ingesta: no queda tema a medias visible como completo.
- Caracteres especiales / tildes / tablas del temario se conservan legibles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST permitir cargar uno o varios ficheros PDF desde disco local.
- **FR-002**: System MUST extraer el texto íntegro de cada PDF conservando orden de lectura y paginación.
- **FR-003**: System MUST dividir cada tema en fragmentos recuperables citando tema y posición.
- **FR-004**: System MUST listar temas cargados con título (inicial = nombre de fichero, editable por el usuario), páginas y estado (listo/procesando/error).
- **FR-005**: System MUST rechazar PDFs sin texto extraíble, corruptos o multi-tema con mensaje accionable que pida dividir el fichero (1 PDF = 1 tema en v1).
- **FR-006**: System MUST permitir reemplazar el PDF de un tema sin duplicar entradas, conservando el título personalizado del usuario.
- **FR-007**: System MUST permitir borrar un tema y todos sus fragmentos asociados.
- **FR-008**: System MUST funcionar sin conexión a internet una vez instalado (todo en local).
- **FR-009**: System MUST impedir que contenido de un tema borrado aparezca en búsquedas.
- **FR-010**: Users MUST be able to consultar el estado de ingesta (progreso) de cada tema.

### Key Entities

- **Tema**: representa un tema de la oposición; atributos: título, origen (nombre fichero), hash SHA-256 del contenido (identidad anti-duplicado), nº páginas, estado, fecha de carga.
- **Fragmento**: porción del texto oficial de un tema; atributos: texto literal, nº orden, referencia de página/sección, tema padre.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Usuarios cargan un temario de 20 temas (PDFs de ~30 páginas) y ven los 20 listos en menos de 30 minutos en un portátil estándar.
- **SC-002**: El 100% de los fragmentos recuperados son texto literal del PDF, con referencia correcta de tema y posición en pruebas sobre 3 temas muestra.
- **SC-003**: 90% de opositores completa la primera carga sin ayuda siguiendo solo los mensajes de la app.
- **SC-004**: El temario sigue consultable tras reiniciar la app y sin internet.

## Assumptions

- PDFs digitales con texto seleccionable en español; OCR de escaneados fuera de alcance v1.
- Un fichero PDF equivale a un tema; multi-tema por PDF diferido.
- Uso monousuario en un solo equipo; sin concurrencia ni multi-idioma en v1.
- Detalles técnicos (Tika, Ollama, SQLite) fijados por la constitution y a concretar en plan, no en esta spec.
