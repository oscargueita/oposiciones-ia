package com.examprep.exam;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeneratedTestRepository extends JpaRepository<GeneratedTest, Long> {
  List<GeneratedTest> findByTopicIdOrderByCreatedAtDesc(Long topicId);
}
