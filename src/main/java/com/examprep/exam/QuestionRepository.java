package com.examprep.exam;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
  List<Question> findByTestIdOrderBySequenceAsc(Long testId);
  long countByTestId(Long testId);
}
