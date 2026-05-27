package me.googas.starbox.logging;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class DebuggingFilter implements Filter {
  @Override
  public boolean isLoggable(LogRecord record) {
    if (record.getLevel().intValue() < Level.INFO.intValue()) {
      record.setLevel(Level.INFO);
    }
    return true;
  }
}
