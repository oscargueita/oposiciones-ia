package com.examprep.exam;

import java.util.List;

/** JSON schema required from the model (via BeanOutputConverter). */
public record QuestionJson(String statement, List<String> options, int correctIndex,
    String explanation) {
}
