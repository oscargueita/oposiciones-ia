-- Mixed tests: tema_id nullable (null = multi-topic) + alcance label.
CREATE TABLE test_generado_new (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  tema_id INTEGER REFERENCES temas(id) ON DELETE CASCADE,
  alcance TEXT NOT NULL DEFAULT 'TEMA',
  dificultad TEXT NOT NULL CHECK (dificultad IN ('EASY','MEDIUM','HARD')),
  num_preguntas INTEGER NOT NULL CHECK (num_preguntas >= 1),
  estado TEXT NOT NULL CHECK (estado IN ('PENDING','GRADED')),
  creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
INSERT INTO test_generado_new
  SELECT id, tema_id, 'TEMA', dificultad, num_preguntas, estado, creado_en
  FROM test_generado;
DROP TABLE test_generado;
ALTER TABLE test_generado_new RENAME TO test_generado;
CREATE INDEX IF NOT EXISTS idx_tests_tema ON test_generado(tema_id);
