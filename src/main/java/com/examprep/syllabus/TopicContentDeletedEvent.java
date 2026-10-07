package com.examprep.syllabus;

/** Published when a topic's content is deleted (deletion or replacement). Cleanup in voice. */
public record TopicContentDeletedEvent(Long topicId) {
}
