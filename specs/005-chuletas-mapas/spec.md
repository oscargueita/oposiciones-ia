# Feature Specification: Chuletas y Mapas

**Feature Branch**: `005-chuletas-mapas`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Crear chuletas de una página y mapas conceptuales por tema para repaso visual"

## Clarifications

### Session 2026-10-07

- Q: ¿Las chuletas y mapas deben guardarse para reutilizar o generarse de nuevo cada vez? → A: Opción A — guardar en base de datos y reutilizar.
- Q: ¿Qué tipo de diagrama debe ser el mapa conceptual? → A: Opción A — mapa mental jerárquico (tema central y ramas).
- Q: ¿En qué formato se debe entregar la chuleta? → A: Opción A — markdown con títulos y listas.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generar chuleta de un tema (Priority: P1)

El opositor pide la chuleta de un tema y recibe en una página los conceptos clave, cifras y plazos con cita de procedencia.

**Why this priority**: Es el repaso visual de última hora antes del examen.

**Independent Test**: Chuleta de la Constitución con 10-20 puntos clave, cada bloque con cita.

**Acceptance Scenarios**:

1. **Given** un tema listo con contenido, **When** el usuario pide su chuleta, **Then** recibe un resumen de una página con puntos clave y citas.
2. **Given** un tema vacío o en error, **When** pide la chuleta, **Then** recibe mensaje claro sin contenido inventado.

---

### User Story 2 - Generar mapa conceptual (Priority: P2)

El opositor pide el mapa de un tema y recibe un diagrama de conceptos y relaciones en formato estándar reutilizable.

**Why this priority**: Ver la estructura de un vistazo fija relaciones que el texto lineal no muestra.

**Independent Test**: Mapa de un tema con 8-15 nodos jerárquicos y sintaxis válida verificable.

**Acceptance Scenarios**:

1. **Given** un tema listo, **When** el usuario pide su mapa, **Then** recibe un diagrama jerárquico con los conceptos del tema.
2. **Given** un diagrama generado, **When** se valida su sintaxis, **Then** es renderizable sin errores.

---

### User Story 3 - Exportar para estudiar offline (Priority: P3)

El opositor descarga la chuleta en texto y el mapa en su formato para guardarlos o imprimirlos.

**Why this priority**: El repaso final suele ser en papel o sin la app delante.

**Independent Test**: Descargar ambos ficheros y abrirlos con herramientas estándar.

**Acceptance Scenarios**:

1. **Given** una chuleta generada, **When** el usuario la exporta, **Then** descarga un fichero de texto legible.
2. **Given** un mapa generado, **When** el usuario lo exporta, **Then** descarga su diagrama reutilizable.

---

### Edge Cases

- Tema con poco contenido: chuleta más corta con aviso, sin relleno inventado.
- Salida del modelo con formato inválido: se reintenta acotado y si falla se informa.
- Tema reemplazado: la chuleta/mapa se regeneran (no hay caché obsoleta).
- Caracteres especiales del texto legal se conservan legibles.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST generar por tema una chuleta de una página en markdown (títulos y listas) con conceptos clave, cifras y plazos.
- **FR-002**: System MUST citar la procedencia (tema + posición) por bloque de la chuleta.
- **FR-003**: System MUST generar por tema un mapa jerárquico de 8-15 nodos con sintaxis válida verificada.
- **FR-004**: System MUST reintentar acotado ante formato inválido e informar si no se logra.
- **FR-005**: System MUST exportar la chuleta como texto y el mapa como diagrama reutilizable.
- **FR-006**: System MUST rechazar temas vacíos, en proceso o en error con mensaje accionable.
- **FR-007**: System MUST funcionar sin conexión a internet (generación local).
- **FR-008**: System MUST basar todo contenido en el texto oficial sin inventar (citas obligatorias).
- **FR-009**: System MUST guardar chuleta y mapa por tema para reutilizarlos sin regenerar, e invalidarlos si el tema cambia o se borra.

### Key Entities

- **Chuleta**: documento persistido por tema (se regenera si el tema cambia); atributos: tema, bloques con puntos y citas, fecha.
- **Mapa**: diagrama persistido por tema (se regenera si el tema cambia); atributos: tema, nodos jerárquicos, formato estándar, fecha.
- Reutiliza **Fragmento** (001) y generación local (004).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 90% de chuletas cubre los conceptos evaluables del tema según revisión manual de 5 temas.
- **SC-002**: El 100% de mapas generados tiene sintaxis válida verificada automáticamente.
- **SC-003**: Usuarios generan chuleta + mapa de un tema en menos de 4 minutos.
- **SC-004**: Generación funciona con la red desconectada.

## Assumptions

- Chuleta en markdown y mapa mental en Mermaid (formatos estándar y reutilizables).
- Chuleta y mapa se persisten por tema y se invalidan si el tema cambia o se borra.
- 8-15 nodos y 1 página como límites razonables v1.
