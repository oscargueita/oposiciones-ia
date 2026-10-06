CREATE TABLE IF NOT EXISTS temas (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  titulo TEXT NOT NULL,
  origen_nombre TEXT NOT NULL,
  content_sha256 CHAR(64) NOT NULL UNIQUE,
  num_paginas INTEGER NOT NULL CHECK (num_paginas > 0),
  estado TEXT NOT NULL CHECK (estado IN ('PROCESANDO','LISTO','ERROR')),
  mensaje_error TEXT,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS fragmentos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  tema_id INTEGER NOT NULL REFERENCES temas(id) ON DELETE CASCADE,
  orden INTEGER NOT NULL CHECK (orden >= 0),
  pagina INTEGER NOT NULL CHECK (pagina >= 1),
  texto TEXT NOT NULL CHECK (length(texto) > 0),
  UNIQUE (tema_id, orden)
);
CREATE INDEX IF NOT EXISTS idx_fragmentos_tema ON fragmentos(tema_id);

CREATE TABLE IF NOT EXISTS chunk_embedding (
  chunk_id INTEGER PRIMARY KEY REFERENCES fragmentos(id) ON DELETE CASCADE,
  embedding BLOB NOT NULL
);
