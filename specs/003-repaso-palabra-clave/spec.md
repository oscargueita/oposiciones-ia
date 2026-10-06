# Feature Specification: Repaso por Palabra Clave

**Feature Branch**: `003-repaso-palabra-clave`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Repasar un concepto por palabra clave reproduciendo por voz el fragmento relacionado"

## Clarifications

### Session 2026-10-07

- Q: ¿La palabra clave se introduce solo escrita o también dictada por voz? → A: Opción A — solo texto escrito en v1, dictado por voz diferido.
- Q: ¿Cuántos fragmentos candidatos debe mostrar el repaso por defecto? → A: Opción B — 5 candidatos.
- Q: ¿El repaso debe buscar por defecto en todo el temario o solo en el último tema estudiado? → A: Opción A — todo el temario por defecto, acotar es opcional.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Escuchar el fragmento de una palabra clave (Priority: P1)

El opositor escribe o dice una palabra clave ("recurso de alzada") y la app reproduce por voz el fragmento del temario más relacionado, indicando tema y posición.

**Why this priority**: Es el repaso rápido central: un concepto → su explicación oficial en voz.

**Independent Test**: Buscar "recurso de alzada" y comprobar que se escucha un fragmento de la LPAC con su cita.

**Acceptance Scenarios**:

1. **Given** temario cargado, **When** el usuario repasa una palabra presente en el temario, **Then** escucha el fragmento más relacionado con cita de tema y posición.
2. **Given** una palabra sin relación con el temario, **When** el usuario la repasa, **Then** recibe un aviso claro de sin resultados en vez de audio inventado.

---

### User Story 2 - Elegir entre varios resultados (Priority: P2)

El opositor recibe los fragmentos más relevantes ordenados y elige cuál escuchar cuando el primero no es el que buscaba.

**Why this priority**: Las palabras ambiguas ("plazo", "recurso") aparecen en muchos temas; imponer uno solo frustra.

**Independent Test**: Buscar "plazo" y comprobar la lista ordenada con al menos 3 candidatos escuchables.

**Acceptance Scenarios**:

1. **Given** una palabra con varios fragmentos relacionados, **When** el usuario la repasa, **Then** ve la lista ordenada por relevancia con cita cada uno.
2. **Given** la lista de candidatos, **When** el usuario elige el segundo, **Then** escucha ese fragmento.

---

### User Story 3 - Acotar el repaso a un tema (Priority: P3)

El opositor limita la búsqueda a un tema concreto cuando repasa esa parte del temario.

**Why this priority**: Evita resultados de temas que aún no estudia y acelera el repaso dirigido.

**Independent Test**: Repasar "plazo" acotado a la Constitución y comprobar que todos los resultados son de ese tema.

**Acceptance Scenarios**:

1. **Given** un tema elegido, **When** el usuario repasa una palabra, **Then** todos los resultados pertenecen a ese tema.
2. **Given** un tema donde la palabra no aparece, **When** el usuario la repasa acotada, **Then** recibe aviso de sin resultados en ese tema.

---

### Edge Cases

- Palabra vacía o de un solo carácter: se rechaza pidiendo al menos 2 caracteres.
- Fragmento relacionado sin audio aún: se prepara al pedirlo y luego se reproduce (espera breve).
- Palabra con caracteres especiales o tildes: "alzada" encuentra "alzada" con o sin tilde.
- Tema borrado tras mostrar resultados: elegirlo informa que ya no existe.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST aceptar una palabra o frase corta escrita y devolver los fragmentos más relacionados del temario (dictado por voz diferido a v2).
- **FR-002**: System MUST reproducir por voz el fragmento elegido con cita de tema y posición.
- **FR-003**: System MUST ordenar los candidatos por relevancia (5 por defecto) y mostrar su cita antes de escuchar.
- **FR-004**: System MUST permitir acotar la búsqueda a un tema concreto (por defecto busca en todo el temario).
- **FR-005**: System MUST avisar sin resultados en vez de inventar contenido cuando nada se relaciona.
- **FR-006**: System MUST preparar el audio si aún no existe y reproducirlo después sin error.
- **FR-007**: System MUST funcionar sin conexión a internet (búsqueda y voz en local).
- **FR-008**: Users MUST be able to ver la cita (tema + posición) de cada candidato.

### Key Entities

- **Resultado de repaso**: candidato efímero (no persistido); atributos: fragmento, relevancia, cita.
- Reutiliza **Fragmento**, **Tema** (001) y audios de **Narración** (002).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 90% de 20 palabras de prueba devuelve como primer resultado un fragmento pertinente del tema esperado.
- **SC-002**: Usuarios escuchan el concepto buscado en menos de 30 segundos desde que lo piden (incluida generación bajo demanda).
- **SC-003**: El 100% de búsquedas sin relación muestra aviso en vez de audio (0 alucinaciones en pruebas).
- **SC-004**: El repaso funciona con la red desconectada tras la instalación.

## Assumptions

- Palabra o frase corta (1-5 palabras); preguntas largas diferidas a la feature de tests/chat.
- N candidatos por defecto razonable (p. ej. 5); paginación diferida.
- Relevancia combina coincidencia literal y similitud de significado (detalle al plan).
- Reutiliza búsqueda de 001 y audios de 002; sin nuevas persistencias salvo lo que pida el plan.
