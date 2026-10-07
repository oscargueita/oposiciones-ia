-- English enum values (were Spanish). Columns keep Spanish names.
CREATE TABLE temas_new (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  titulo TEXT NOT NULL,
  origen_nombre TEXT NOT NULL,
  content_sha256 CHAR(64) NOT NULL UNIQUE,
  num_paginas INTEGER NOT NULL CHECK (num_paginas > 0),
  estado TEXT NOT NULL CHECK (estado IN ('PROCESSING','READY','FAILED')),
  mensaje_error TEXT,
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO temas_new
  SELECT id, titulo, origen_nombre, content_sha256, num_paginas,
    CASE estado WHEN 'PROCESANDO' THEN 'PROCESSING' WHEN 'LISTO' THEN 'READY' ELSE 'FAILED' END,
    mensaje_error, creado_en FROM temas;
DROP TABLE temas;
ALTER TABLE temas_new RENAME TO temas;

CREATE TABLE test_generado_new (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  tema_id INTEGER NOT NULL REFERENCES temas(id) ON DELETE CASCADE,
  dificultad TEXT NOT NULL CHECK (dificultad IN ('EASY','MEDIUM','HARD')),
  num_preguntas INTEGER NOT NULL CHECK (num_preguntas >= 1),
  estado TEXT NOT NULL CHECK (estado IN ('PENDING','GRADED')),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO test_generado_new
  SELECT id, tema_id,
    CASE dificultad WHEN 'FACIL' THEN 'EASY' WHEN 'MEDIO' THEN 'MEDIUM' ELSE 'HARD' END,
    num_preguntas,
    CASE estado WHEN 'PENDIENTE' THEN 'PENDING' ELSE 'GRADED' END,
    creado_en FROM test_generado;
DROP TABLE test_generado;
ALTER TABLE test_generado_new RENAME TO test_generado;
CREATE INDEX IF NOT EXISTS idx_tests_tema ON test_generado(tema_id);
