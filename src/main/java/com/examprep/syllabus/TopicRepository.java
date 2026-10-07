package com.examprep.syllabus;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
  Optional<Topic> findByContentSha256(String sha256);
}
