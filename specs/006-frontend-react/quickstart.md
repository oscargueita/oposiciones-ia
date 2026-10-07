# Quickstart: Frontend React (006)

**Prerrequisitos**: Node 22 LTS (`fnm install 22` o paquete oficial), backend con temario, `frontend/` creado.

## Validación end-to-end

1. Instalar Node 22 y `cd frontend && npm install`.
2. Arrancar backend (`./mvnw spring-boot:run`) y dev (`npm run dev` en :5173).
3. Abrir :5173 → lista de temas visible con los 83 cargados.
4. Subir un PDF desde la vista Temas → aparece LISTO.
5. Reproducir un tema: avanza solo, pausar, recargar, continuar en el punto.
6. Repaso: buscar "plazo", ver 5 citas, escuchar una.
7. Tests: generar 3, responder 1x1 con feedback, ver nota e historial.
8. Material: ver chuleta renderizada y mapa dibujado de un tema.
9. Build: `npm run build` + copiar a `static/` → todo funciona en :8080 sin :5173.
10. Offline (red cortada, localhost intacto): repetir 3-8 → idéntico.

**Esperado**: SC-001–SC-004 de `spec.md`.
