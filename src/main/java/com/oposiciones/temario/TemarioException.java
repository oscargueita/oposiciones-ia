package com.oposiciones.temario;

public class TemarioException extends RuntimeException {
  private final int status;

  public TemarioException(String message, int status) {
    super(message);
    this.status = status;
  }

  public TemarioException(String message, int status, Throwable cause) {
    super(message, cause);
    this.status = status;
  }

  public int getStatus() { return status; }
}
