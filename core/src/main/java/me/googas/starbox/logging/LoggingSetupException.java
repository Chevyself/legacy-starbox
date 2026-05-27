package me.googas.starbox.logging;

public class LoggingSetupException extends RuntimeException {

  public LoggingSetupException(String message, Throwable cause) {
    super(message, cause);
  }

  public LoggingSetupException(String message) {
    super(message);
  }
}
