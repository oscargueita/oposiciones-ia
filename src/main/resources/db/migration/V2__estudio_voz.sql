CREATE TABLE IF NOT EXISTS fragmento_audio (
  fragmento_id INTEGER PRIMARY KEY REFERENCES fragmentos(id) ON DELETE CASCADE,
  duracion_seg REAL NOT NULL CHECK (duracion_seg > 0),
  generado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS progreso_escucha (
  tema_id INTEGER PRIMARY KEY REFERENCES temas(id) ON DELETE CASCADE,
  fragmento_id INTEGER NOT NULL,
  offset_seg REAL NOT NULL CHECK (offset_seg >= 0),
  actualizado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
