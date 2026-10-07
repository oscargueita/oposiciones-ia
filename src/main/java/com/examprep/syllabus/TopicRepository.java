package com.examprep.syllabus;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
  Optional<Topic> findByContentHash(ContentHash contentHash);
}
