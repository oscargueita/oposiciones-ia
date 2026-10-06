# Feature Specification: Estudio Tema por Voz

**Feature Branch**: `002-estudio-tema-voz`

**Created**: 2026-10-07

**Status**: Draft

**Input**: User description: "Estudiar un tema escuchando toda su información por voz (TTS local)"

## Clarifications

### Session 2026-10-07

- Q: ¿Cómo debe escuchar el usuario la narración si aún no hay pantalla ni app móvil, solo el servicio? → A: Opción B — audio por fragmento servido en orden para reproducir por partes.
- Q: ¿Al reanudar debe continuar desde el segundo exacto o desde el inicio del fragmento actual? → A: Opción B — desde el segundo exacto donde se pausó.
- Q: ¿La primera escucha de un tema puede tardar en prepararse o debe empezar al instante? → A: Opción C — audios preparados en segundo plano tras la ingesta, escucha inmediata.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Escuchar un tema entero (Priority: P1)

El opositor elige un tema cargado y pulsa escuchar. La app reproduce por voz todo el contenido oficial del tema, de principio a fin, en orden de lectura.

**Why this priority**: Es el caso central de la feature y la forma principal de estudiar sin mirar la pantalla.

**Independent Test**: Elegir un tema de 3 páginas y escuchar su narración completa verificando que cubre todo el texto en orden.

**Acceptance Scenarios**:

1. **Given** un tema en estado listo, **When** el usuario inicia la escucha, **Then** la narración reproduce íntegro el texto oficial en orden.
2. **Given** un tema inexistente o en error, **When** el usuario intenta escucharlo, **Then** ve un mensaje claro y no se inicia ninguna narración.

---

### User Story 2 - Pausar, reanudar y reiniciar (Priority: P2)

El opositor pausa la narración cuando le interrumpen, la reanuda por donde iba y puede reiniciarla desde el principio cuando quiere repasar.

**Why this priority**: Los temas son largos; sin controles la escucha no sirve para sesiones reales de estudio.

**Independent Test**: Iniciar escucha, pausar a mitad, reanudar y comprobar que continúa en el mismo punto; reiniciar y comprobar que vuelve al principio.

**Acceptance Scenarios**:

1. **Given** una narración en curso, **When** el usuario pausa y reanuda, **Then** continúa en el mismo punto sin repetir ni saltar contenido.
2. **Given** una narración pausada o terminada, **When** el usuario reinicia, **Then** la narración vuelve al principio del tema.

---

### User Story 3 - Continuar donde lo dejó (Priority: P3)

El opositor cierra la app a mitad de un tema y al volver puede continuar desde el punto donde se quedó, sin buscar manualmente.

**Why this priority**: El estudio se hace en varias sesiones; perder el punto obliga a reescuchar o a tomar notas externas.

**Independent Test**: Pausar a mitad, cerrar y reabrir, y comprobar que la app ofrece continuar desde el punto guardado.

**Acceptance Scenarios**:

1. **Given** una escucha interrumpida por cierre, **When** el usuario vuelve al tema, **Then** la app muestra el punto guardado y ofrece continuar desde ahí.
2. **Given** una escucha terminada, **When** el usuario vuelve al tema, **Then** no se ofrece continuar (parte desde el principio).

---

### Edge Cases

- Tema vacío o sin fragmentos: se informa "sin contenido narrable" y no se genera audio.
- Tema reemplazado mientras hay progreso guardado: el progreso anterior se invalida con aviso.
- Tema borrado con progreso guardado: el progreso desaparece con el tema.
- Interrupción a mitad de generación de audio: al reintentar no quedan ficheros a medias visibles como válidos.
- Caracteres no pronunciables (tablas, símbolos): se verbalizan de forma legible o se omiten sin romper la narración.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST permitir elegir un tema listo y iniciar su narración completa por voz, servida como audios por fragmento en orden de lectura.
- **FR-002**: System MUST narrar el texto oficial íntegro en orden de lectura, sin resúmenes ni añadidos, con un audio por fragmento encadenable.
- **FR-003**: System MUST permitir pausar y reanudar la narración en el segundo exacto donde se pausó (fragmento + offset en segundos).
- **FR-004**: System MUST permitir reiniciar la narración desde el principio.
- **FR-005**: System MUST guardar el punto de escucha por tema (fragmento + segundo) y ofrecer continuar tras cerrar la app.
- **FR-006**: System MUST invalidar el progreso guardado si el contenido del tema cambia o se borra.
- **FR-007**: System MUST funcionar sin conexión a internet (voz y contenido en local).
- **FR-008**: Users MUST be able to saber en qué punto de la narración están (progreso visible).
- **FR-009**: System MUST rechazar narrar temas vacíos, en proceso o en error con mensaje accionable.
- **FR-010**: System MUST preparar los audios de los fragmentos en segundo plano tras la ingesta para que la escucha empiece al instante.

### Key Entities

- **Narración**: representa la escucha de un tema; atributos: tema asociado, punto actual (fragmento + segundo), estado (en curso/pausada/terminada).
- **Progreso de escucha**: punto guardado por tema; atributos: tema, fragmento, segundo, fecha; se invalida si el tema cambia.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Usuarios escuchan un tema de 30 páginas de principio a fin sin cortes ni contenido inventado.
- **SC-002**: El 100% de las pausas/reanudaciones continúan en el mismo punto en pruebas sobre 5 sesiones.
- **SC-003**: Tras cerrar la app a mitad de tema, el 90% de usuarios continúa desde el punto guardado sin ayuda.
- **SC-004**: La escucha funciona con la red desconectada tras la instalación.

## Assumptions

- Un tema se narra entero en secuencia; navegación por fragmentos concretos diferida a la feature de repaso por palabra clave.
- Velocidad/voz configurables diferidas a v2 salvo default razonable del sistema.
- Reutiliza Tema/Fragmento de la feature 001; detalles de motor de voz y formato de audio al plan (constitution: `TtsService` desacoplado).
