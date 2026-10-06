package com.oposiciones.temario;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FragmentoRepository extends JpaRepository<Fragmento, Long> {
  List<Fragmento> findByTemaIdOrderByOrdenAsc(Long temaId);
  long countByTemaId(Long temaId);
  void deleteByTemaId(Long temaId);
}
