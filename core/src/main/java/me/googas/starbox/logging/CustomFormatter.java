package me.googas.starbox.logging;

import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import lombok.NonNull;

public class CustomFormatter extends Formatter {

  @NonNull private final String format;

  public CustomFormatter(@NonNull String format) {
    this.format = format;
  }

  public CustomFormatter() {
    this.format = "[%1$tT] [%2$s/%3$s]: %4$s\n";
  }

  @Override
  public String format(LogRecord record) {
    String message =
        String.format(
            this.format,
            record.getMillis(),
            getThreadName(record.getThreadID()),
            record.getLevel().getName(),
            record.getMessage());
    if (record.getThrown() == null) {
      return message;
    } else {
      StringBuilder builder = new StringBuilder(message);
      Throwable lastCause = record.getThrown();
      while (lastCause != null) {
        builder
            .append(lastCause.getClass().getName())
            .append(": ")
            .append(lastCause.getMessage())
            .append("\n");
        for (StackTraceElement element : record.getThrown().getStackTrace()) {
          builder.append("    ").append(element.toString()).append("\n");
        }
        lastCause = lastCause.getCause();
      }
      return builder.toString();
    }
  }

  @NonNull
  private String getThreadName(long id) {
    return Thread.getAllStackTraces().keySet().stream()
        .filter(thread -> thread.getId() == id)
        .findFirst()
        .map(Thread::getName)
        .orElse("Anonymous " + id);
  }
}
