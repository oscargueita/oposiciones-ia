package com.examprep.syllabus;

public class SyllabusException extends RuntimeException {
  private final int status;

  public SyllabusException(String message, int status) {
    super(message);
    this.status = status;
  }

  public SyllabusException(String message, int status, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public int getStatus() { return status; }
}
