package com.examprep.syllabus;

/** Published when a topic becomes READY (ingestion or replacement). Consumed by the voice module. */
public record TopicReadyEvent(Long topicId) {
}
