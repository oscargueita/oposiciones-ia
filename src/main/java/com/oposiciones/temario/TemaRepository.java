package com.oposiciones.temario;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemaRepository extends JpaRepository<Tema, Long> {
  Optional<Tema> findByContentSha256(String sha256);
}
