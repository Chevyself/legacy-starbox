package me.googas.starbox.logging;

import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.NonNull;

public class LoggerOutputStream extends OutputStream {

  @NonNull private final Logger logger;
  @NonNull private final Level level;
  @NonNull private final String prefix;
  @NonNull private final StringBuilder buffer;

  public LoggerOutputStream(@NonNull Logger logger, @NonNull Level level, @NonNull String prefix) {
    this.logger = logger;
    this.level = level;
    this.prefix = prefix;
    this.buffer = new StringBuilder();
  }

  @Override
  public void write(int b) {
    char c = (char) b;
    if (c == '\n') {
      logger.log(this.level, "[" + prefix + "] " + this.buffer);
      buffer.setLength(0);
    } else {
      buffer.append(c);
    }
  }
}
