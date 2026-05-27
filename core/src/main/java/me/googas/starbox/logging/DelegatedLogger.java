package me.googas.starbox.logging;

import java.util.logging.LogRecord;
import java.util.logging.Logger;
import lombok.NonNull;

public class DelegatedLogger extends Logger {

  @NonNull private final String formattedName;

  protected DelegatedLogger(@NonNull Logger parent, @NonNull String name) {
    super(name, null);
    this.formattedName = "[" + name + "] ";
    this.setParent(parent);
    this.setFilter(parent.getFilter());
  }

  @Override
  public void log(LogRecord record) {
    record.setMessage(this.formattedName + record.getMessage());
    super.log(record);
  }
}
