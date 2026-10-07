package com.examprep.syllabus;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FragmentRepository extends JpaRepository<Fragment, Long> {
  List<Fragment> findByTopicIdOrderBySequenceAsc(Long topicId);
  long countByTopicId(Long topicId);
  void deleteByTopicId(Long topicId);
}
