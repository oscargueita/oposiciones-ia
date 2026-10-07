# Research: Frontend React (006)

**Fecha**: 2026-10-07 | **Base**: API 001-005. Sin entidades nuevas.

## Decision 1: Vite + React + TypeScript en `frontend/`

- **Decision**: Vite 6 + React 18 + TypeScript en `frontend/` (Node 22 LTS requerido; la máquina actual tiene Node 12 → instalar vía fnm o paquete oficial antes de implementar).
- **Rationale**: Estándar actual, dev rápido con proxy, build estático servible por Boot.
- **Alternatives considered**: Create React App (obsoleto); Thymeleaf/HTMX (peor reproductor, descartado con el usuario).

## Decision 2: React Router con 4 vistas

- **Decision**: `react-router-dom` con rutas `/temas`, `/repaso`, `/tests`, `/material` (chuleta+mapa), menú superior persistente.
- **Rationale**: Implementa la aclaración de vistas separadas.
- **Alternatives considered**: Single-page con scroll (rechazado en clarify); estado manual sin router (peor deep-linking).

## Decision 3: Reproductor con `<audio>` + autoavance + offset

- **Decision**: Elemento `<audio>` por fragmento; al evento `ended` avanza al siguiente (aclaración autoplay); al pausar/antes de salir guarda `{fragmentoId, currentTime}` vía `PUT /progreso`; al entrar lee `GET /progreso` y ofrece continuar (seek al offset al cargar metadata).
- **Rationale**: Sin dependencias, control exacto del segundo, funciona offline contra backend local.
- **Alternatives considered**: Howler.js (innecesario para secuencial simple); MediaSession API diferida a v2.

## Decision 4: Render de markdown y Mermaid con librerías

- **Decision**: `react-markdown` para chuletas; `mermaid` (API `mermaid.render`) para mapas mindmap.
- **Rationale**: Estándar, sin backend extra, reutiliza formatos ya generados.
- **Alternatives considered**: Texto plano (peor lectura, rechazado); render propio de grafos (YAGNI).

## Decision 5: Mismo origen en producción, proxy en desarrollo

- **Decision**: Boot sirve `frontend/dist` desde `src/main/resources/static` (copia en build vía `frontend-maven-plugin` o script); en dev Vite proxy `/api → localhost:8080`. Sin CORS en producción; CORS abierto solo en perfil dev si hiciera falta.
- **Rationale**: Una URL, cero CORS en uso real, despliegue en un solo proceso.
- **Alternatives considered**: Dos orígenes + CORS siempre (más piezas móviles, rechazado).
