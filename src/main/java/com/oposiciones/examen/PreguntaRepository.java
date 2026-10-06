package com.oposiciones.examen;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
  List<Pregunta> findByTestIdOrderByOrdenAsc(Long testId);
  long countByTestId(Long testId);
}
