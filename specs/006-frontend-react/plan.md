# Implementation Plan: Frontend React

**Branch**: `006-frontend-react` | **Date**: 2026-10-07 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/006-frontend-react/spec.md` + stack acordado: Vite + React + TS en `frontend/`, React Router 4 vistas, `<audio>` con autoplay, `react-markdown` + `mermaid`, Boot sirve el build.

## Summary

SPA en `frontend/` (Vite 6, React 18, TS, Router, 4 vistas) que consume la API 001-005: Temas (lista+subida+estudio con reproductor y continuar), Repaso, Tests (1x1+nota+historial) y Material (chuleta renderizada + mapa dibujado). Producción en misma URL (Boot sirve `dist`); dev con proxy.

## Technical Context

**Language/Version**: TypeScript 5 + Node 22 LTS (instalar: la máquina tiene Node 12).

**Primary Dependencies**: `react`, `react-router-dom`, `react-markdown`, `mermaid`; dev: `vite`, `@vitejs/plugin-react`, `typescript`.

**Storage**: Sin persistencia nueva (estado en cliente; progreso en backend).

**Testing**: Vitest + Testing Library para componentes; Playwright diferido (validación manual quickstart en v1).

**Target Platform**: Navegador moderno en macOS/Linux contra `localhost:8080`.

**Project Type**: Web app (carpeta `frontend/` + servido estático por Boot).

**Performance Goals**: Carga inicial < 3 s; avance de fragmento sin cortes (precarga del siguiente audio).

**Constraints**: 100% local; misma URL en producción; TDD adaptado (tests de componentes donde aporten).

**Scale/Scope**: 4 vistas; sin login; sin PWA en v1.

## Constitution Check

- [x] I Local-First: todo localhost. PASS.
- [x] II Java 21 + Boot 3: Boot intacto + sirve estático. PASS.
- [x] III-VII: sin cambios de dominio. PASS.
- [x] VIII English Code: código y comentarios del frontend en inglés; UI visible en español. PASS.

Post-Phase-1 re-check: sin violaciones; Complexity Tracking vacía.

## Project Structure

### Documentation (this feature)

```text
specs/006-frontend-react/
├── plan.md              # This file (/speckit.plan command output)
├── research.md          # Phase 0 output (/speckit.plan command)
├── data-model.md        # Phase 1 output (/speckit.plan command)
├── quickstart.md        # Phase 1 output (/speckit.plan command)
├── contracts/           # Phase 1 output (/speckit.plan command)
│   └── ui-api.yaml
└── tasks.md             # Phase 2 output (/speckit.tasks command - NOT created by /speckit.plan)
```

### Source Code (repository root)

```text
frontend/
├── package.json (dev proxy /api → 8080, build → dist/)
├── src/
│   ├── App.tsx (Router + menú)
│   ├── api.ts (cliente fetch tipado)
│   ├── views/Temas.tsx, Estudio.tsx (player), Repaso.tsx, Tests.tsx, Material.tsx
│   └── components/Player.tsx (audio + autoavance + progreso)
└── dist/ (gitignored, copiado a src/main/resources/static en build)
src/main/resources/static/ (salida del build, gitignored)
```

**Structure Decision**: `frontend/` separada en el mismo repo; Boot sirve el build para URL única.

## Complexity Tracking

> Sin violaciones — tabla vacía intencionadamente.
