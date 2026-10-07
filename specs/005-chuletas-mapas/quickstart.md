# Quickstart: Chuletas y Mapas (005)

**Prerrequisitos**: App con temario cargado + Ollama `llama3.1:8b`.

## Validación end-to-end

1. Chuleta: `curl http://localhost:8080/api/v1/temas/2/chuleta` → markdown con secciones y citas `(tema, pág. N)`.
2. Segunda llamada idéntica → misma chuleta sin regenerar (persistida).
3. Mapa: `curl http://localhost:8080/api/v1/temas/2/mapa` → bloque `mindmap` con 8-15 nodos.
4. Pegar el mapa en https://mermaid.live → renderiza sin errores.
5. Reemplazar PDF del tema → chuleta y mapa se invalidan (siguiente GET regenera).
6. Tema inexistente → 404; tema en error → 422.
7. Offline: repetir 1-4 sin red → idéntico.
8. Tests: `mvn -B test` (chat mockeado).

**Esperado**: SC-001–SC-004 de `spec.md`.
