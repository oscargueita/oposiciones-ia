# Research: Chuletas y Mapas (005)

**Fecha**: 2026-10-07 | **Base**: 001 (fragmentos), 004 (chat local + validación). Sin audio.

## Decision 1: Chuleta markdown con citas por bloque

- **Decision**: Prompt por tema (muestra de fragmentos clave: primero/últimos + muestreo) que exige markdown con `##` secciones, viñetas con cifras/plazos y cita `(tema, pág. N)` por bloque. Validación: no vacío, ≥5 viñetas, contiene citas con formato; reintento ≤3.
- **Rationale**: Implementa FR-001/002 con la misma maquinaria probada en 004 (chat + validador + reintento).
- **Alternatives considered**: Chuleta por fragmento concatenado (pierde visión global, rechazado).

## Decision 2: Mapa mental Mermaid con validación sintáctica

- **Decision**: Prompt que exige bloque `mindmap` Mermaid (raíz = tema, 8-15 nodos, indentación con espacios, sin caracteres `()[]{};#` en etiquetas). Validación propia sin dependencias: empieza por `mindmap`, indentación monótona por niveles, 8-15 nodos, paréntesis balanceados; reintento ≤3.
- **Rationale**: Mermaid mindmap es renderizable en GitHub/docs; el validador propio evita librerías JS en Java.
- **Alternatives considered**: Validar con Node+mermaid-cli (proceso externo, rompe local-first simple, rechazado); flowchart (menos legible para repaso, rechazado en clarify).

## Decision 3: Persistencia 1:1 con invalidación por eventos

- **Decision**: Tablas `chuleta(tema_id PK, markdown, creado_en)` y `mapa(tema_id PK, mermaid, creado_en)`; `GET` genera si falta; `TopicContentDeletedEvent` (001) borra ambas filas (reutiliza listener de voz ampliado o listener propio).
- **Rationale**: Aclaración A con invalidación automática; 1:1 evita historial de versiones en v1.
- **Alternatives considered**: Sin persistencia (rechazado en clarify); versionado múltiple (YAGNI).
