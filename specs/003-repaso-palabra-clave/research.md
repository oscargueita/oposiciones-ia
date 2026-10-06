# Research: Repaso por Palabra Clave (003)

**Fecha**: 2026-10-07 | **Base**: 001 (Fragmento + embeddings) y 002 (audios bajo demanda). Sin persistencia nueva.

## Decision 1: Ranking híbrido literal + semántico

- **Decision**: score = 0.5·coseno(embedding) + 0.5·bonus literal (1.0 si el texto normalizado contiene la consulta exacta, 0.3 si contiene todas las palabras sueltas, 0.0 si no). Normalización: minúsculas + sin tildes (NFD) en ambos lados. Top-5 por score; si el mejor score < 0.35 ⇒ sin resultados (FR-005).
- **Rationale**: Lo literal ("recurso de alzada" exacto) gana precisión jurídica; lo semántico rescata sinónimos ("impugnación"); el umbral evita alucinaciones. Todo en memoria sobre ~3600 vectores (<50ms).
- **Alternatives considered**: Solo coseno (pierde citas literales exactas); solo LIKE SQL (pierde sinónimos); BM25 externo (infra extra, rechazado).

## Decision 2: Ámbito y validación de entrada

- **Decision**: Parámetro opcional `temaId`; si se da, filtrar fragmentos antes de rankear (404 si el tema no existe). Consulta 2-500 caracteres, se rechaza vacía (FR edge). Tildes insensibles por normalización NFD.
- **Rationale**: Implementa aclaraciones (5 candidatos, ámbito global por defecto, acotado opcional, texto v1).
- **Alternatives considered**: Acotado obligatorio (peor UX de repaso transversal, rechazado en clarify).

## Decision 3: Audio bajo demanda reutilizando 002

- **Decision**: Cada candidato enlaza `audioUrl` de 002 (`/fragmentos/{id}/audio`), que genera el WAV si falta. Sin filas nuevas: `ResultadoRepaso` es efímero (record en memoria).
- **Rationale**: Cero migración, cero duplicación; FR-006 cumplido por el fallback perezoso existente.
- **Alternatives considered**: Pre-generar al buscar (latencia impredecible); tabla de caché de búsquedas (YAGNI, rechazado).
