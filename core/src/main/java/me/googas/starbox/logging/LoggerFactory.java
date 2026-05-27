package me.googas.starbox.logging;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.function.Consumer;
import java.util.logging.*;
import lombok.NonNull;
import me.googas.starbox.CoreFiles;

public class LoggerFactory {

  private static Logger logger;

  public LoggerFactory(@NonNull Logger logger) {
    LoggerFactory.logger = logger;
  }

  @NonNull
  public static Logger getLogger() {
    if (LoggerFactory.logger == null) {
      throw new LoggingSetupException("Logger has not been created");
    }
    return LoggerFactory.logger;
  }

  @NonNull
  public static Logger getLogger(@NonNull Class<?> clazz) {
    return LoggerFactory.getLogger(clazz.getSimpleName());
  }

  @NonNull
  public static Logger getLogger(@NonNull String name) {
    if (LoggerFactory.logger != null) {
      return new DelegatedLogger(logger, name);
    }
    return LoggerFactory.start(name, false);
  }

  @NonNull
  public static Logger start(@NonNull String name, boolean debug, @NonNull Handler... handlers) {
    if (LoggerFactory.logger != null) {
      throw new LoggingSetupException("Logger has already been created");
    }
    Logger logger = Logger.getLogger(name);
    LoggerFactory.logger = logger;
    if (debug) {
      logger.setLevel(Level.ALL);
      logger.setFilter(new DebuggingFilter());
    }

    logger.setUseParentHandlers(false);
    for (Handler handler : handlers) {
      logger.addHandler(handler);
    }

    return logger;
  }

  @NonNull
  public static Handler createConsoleHandler(@NonNull Formatter formatter) {
    ConsoleHandler handler = new ConsoleHandler();
    handler.setFormatter(formatter);
    return handler;
  }

  public static Handler createFileHandler(@NonNull Formatter formatter, String dir, String fileName)
      throws IOException {
    File file = new File(dir, fileName);
    if (!CoreFiles.createAll(file)) {
      throw new IOException("Could not create log file");
    }
    FileHandler handler = new FileHandler(file.toPath().toString(), true);
    handler.setFormatter(formatter);
    return handler;
  }

  public static void supportUncaughtExceptions(@NonNull Logger logger) {
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, throwable) -> {
          logger.log(
              Level.SEVERE,
              "Unchecked exception in thread \"" + thread.getName() + "\"",
              throwable);
        });
  }

  @NonNull
  public static Consumer<? super Throwable> handleException() {
    return LoggerFactory.handleException(null);
  }

  @NonNull
  public static Consumer<? super Throwable> handleException(String message) {
    return throwable ->
        logger.log(Level.WARNING, message != null ? message : "Exception thrown", throwable);
  }

  public static void acceptException(@NonNull Throwable throwable) {
    acceptException(throwable, null);
  }

  public static void acceptException(@NonNull Throwable throwable, String message) {
    LoggerFactory.handleException(message).accept(throwable);
  }

  public static void redirectSystemOut(@NonNull Logger logger) {
    LoggerOutputStream outputStream = new LoggerOutputStream(logger, Level.FINEST, "STDOUT");
    PrintStream printStream = new PrintStream(outputStream, true);
    System.setOut(printStream);
  }

  public static void redirectSystemErr(@NonNull Logger logger) {
    LoggerOutputStream outputStream = new LoggerOutputStream(logger, Level.WARNING, "STDERR");
    PrintStream printStream = new PrintStream(outputStream, true);
    System.setErr(printStream);
  }
}
