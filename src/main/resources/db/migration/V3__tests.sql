CREATE TABLE IF NOT EXISTS test_generado (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  tema_id INTEGER NOT NULL REFERENCES temas(id) ON DELETE CASCADE,
  dificultad TEXT NOT NULL CHECK (dificultad IN ('FACIL','MEDIO','DIFICIL')),
  num_preguntas INTEGER NOT NULL CHECK (num_preguntas >= 1),
  estado TEXT NOT NULL CHECK (estado IN ('PENDIENTE','CORREGIDO')),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_tests_tema ON test_generado(tema_id);

CREATE TABLE IF NOT EXISTS pregunta (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  test_id INTEGER NOT NULL REFERENCES test_generado(id) ON DELETE CASCADE,
  orden INTEGER NOT NULL CHECK (orden >= 0),
  enunciado TEXT NOT NULL CHECK (length(enunciado) > 0),
  opciones TEXT NOT NULL CHECK (length(opciones) > 0),
  correcta INTEGER NOT NULL CHECK (correcta BETWEEN 0 AND 3),
  explicacion TEXT NOT NULL CHECK (length(explicacion) > 0),
  cita_tema_id INTEGER NOT NULL,
  cita_fragmento_id INTEGER NOT NULL,
  cita_pagina INTEGER NOT NULL,
  UNIQUE (test_id, orden)
);
CREATE INDEX IF NOT EXISTS idx_pregunta_test ON pregunta(test_id);

CREATE TABLE IF NOT EXISTS respuesta (
  pregunta_id INTEGER PRIMARY KEY REFERENCES pregunta(id) ON DELETE CASCADE,
  opcion INTEGER NOT NULL CHECK (opcion BETWEEN 0 AND 3),
  acierto INTEGER NOT NULL CHECK (acierto IN (0, 1))
);
