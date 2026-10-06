# Research: Ingesta PDF Temario (001)

**Fecha**: 2026-10-06 | **Stack**: Java 21, Spring Boot 3.3, Spring AI + Ollama local, Tika, SQLite + Flyway

## Decision 1: Spring AI + Ollama

- **Decision**: `spring-ai-bom:1.0.3` + starter `spring-ai-ollama-spring-boot-starter` (aporta `OllamaChatModel` y `OllamaEmbeddingModel`). Config: `spring.ai.ollama.base-url=http://localhost:11434`, chat `llama3.1:8b`, embedding `nomic-embed-text`.
- **Rationale**: Starter único, Boot 3.3 compatible (1.0.x apunta a 3.4/3.5 pero funciona en 3.3). Evitar 0.8.x (viejo) y 2.0.x (exige Boot 4).
- **Alternatives considered**: Llamadas REST manuales a Ollama (más código, sin abstracción chat/embeddings); LangChain4j (válido pero Spring AI integra mejor con Boot).

## Decision 2: Extracción PDF con Tika

- **Decision**: `org.apache.tika:tika-core:3.3.2` + `tika-parser-pdf-module:3.3.2` (huella mínima solo-PDF). Parseo vía `AutoDetectParser` con `BodyContentHandler` para conservar orden y nº página.
- **Rationale**: Tika 3.3.2 estable en Java 21; Tika 4.x cambia defaults (Markdown, parsing out-of-process) innecesarios en v1.
- **Alternatives considered**: PDFBox directo (más control, más código para detectar escaneados/metadatos); Tika 4.1.0 (rechazado por inestabilidad de API en v1).

## Decision 3: SQLite + JPA + Flyway

- **Decision**: Driver `org.xerial:sqlite-jdbc:3.49+`, dialecto `org.hibernate.community.dialect.SQLiteDialect` vía `hibernate-community-dialects` (versión gestionada por Boot), `jdbc:sqlite:oposiciones.db` + WAL, migraciones Flyway SQL simples (DDL SQLite limitado).
- **Rationale**: Cero infraestructura, fichero local versionable fuera de git, cumple constitution local-first.
- **Alternatives considered**: H2 (más cómodo en tests pero fichero menos portable para el usuario); Postgres+pgvector (rechazado: exige servidor externo).

## Decision 4: Vectores sin servidor vectorial

- **Decision**: Tabla `chunk_embedding(chunk_id, embedding BLOB)` con 768 floats little-endian (3072 bytes/vector, ~6MB para 2000 chunks). Búsqueda brute-force en Java con vectores pre-normalizados (coseno = dot-product), top-K en memoria (<10ms/consulta).
- **Rationale**: A escala de oposición (~1200-2000 chunks) no se necesita extensión nativa ni Chroma/Qdrant; simplifica despliegue local a un solo fichero.
- **Alternatives considered**: `sqlite-vec`/`sqlite-vss` (nativo más rápido pero complica JDBC); Chroma/Qdrant embebido (proceso extra, rompe simplicidad v1). Migrable si el temario supera 50k chunks.

## Decision 5: Chunking para temario jurídico

- **Decision**: Chunks ~2000 caracteres con overlap 200, corte recursivo por párrafo/frase sin partir artículos, metadatos `tema_id/orden/pagina`. Detección multi-tema v1 por heurística (nº de patrones "TEMA \d+" distintos > 1 → rechazar).
- **Rationale**: 500-800 tokens equilibra precisión de cita y contexto para estudio/repaso; overlap conserva continuidad entre artículos.
- **Alternatives considered**: Chunks de 4000 caracteres (peor precisión de cita); chunking semántico por embeddings (costoso en ingesta, diferido).

## Decision 6: Detección de duplicados y título

- **Decision**: SHA-256 del bytes del PDF como identidad (`temas.content_sha256 UNIQUE`); título inicial = nombre de fichero sin extensión, editable y conservado en reemplazos.
- **Rationale**: Implementa directamente las 5 aclaraciones de la spec (B/A/B/A/A).
- **Alternatives considered**: Comparar solo por nombre (frágil ante renombres); extraer título del contenido (impredecible en v1).
