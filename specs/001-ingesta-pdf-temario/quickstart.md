# Quickstart: Ingesta PDF Temario (001)

**Prerrequisitos**: Java 21, Ollama corriendo con `nomic-embed-text` (`ollama list`), `data/temario/` con 2 PDFs de prueba (1 válido + 1 duplicado con otro nombre).

## Validación end-to-end

1. Arrancar: `./mvnw spring-boot:run` (usa `oposiciones.db` local en la raíz; Flyway migra solo).
2. Subir tema:
   ```bash
   curl -F "files=@data/temario/tema01.pdf" http://localhost:8080/api/v1/temas
   # → 201 con id, title="topic01", estado PROCESANDO→LISTO
   ```
3. Listar: `curl http://localhost:8080/api/v1/temas` → el tema aparece LISTO con páginas y nº fragmentos.
4. Fidelidad: `curl "http://localhost:8080/api/v1/buscar?q=<frase literal pág.3>"` → devuelve el fragmento con `tema_id + página` correctos.
5. Duplicado: subir el mismo PDF renombrado → `422` "ya cargado (mismo contenido)".
6. Multi-tema: subir PDF con "TEMA 1...TEMA 5" → `422` "divide el fichero, 1 PDF = 1 tema".
7. Renombrar + reemplazar: `PATCH /temas/{id}/titulo` → título custom; `PUT /temas/{id}/pdf` con v2 → título conservado, contenido nuevo.
8. Borrar: `DELETE /temas/{id}` → desaparece del listado y de `/buscar`.
9. Offline: parar red, reiniciar app → listado y búsqueda siguen funcionando.
10. Tests: `./mvnw test` (unit + `MockMvc` + ingesta real con PDFs de `src/test/resources`, embeddings stub).

**Esperado**: SC-001–SC-004 de `spec.md` cumplidos; `oposiciones.db` autocontenido y borrable para reset.
