package com.oposiciones.examen;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestGeneradoRepository extends JpaRepository<TestGenerado, Long> {
  List<TestGenerado> findByTemaIdOrderByCreadoEnDesc(Long temaId);
}
