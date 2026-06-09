package me.googas.net.sockets.json;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class JsonScheduler {
  static final ScheduledExecutorService INSTANCE = Executors.newSingleThreadScheduledExecutor();
}
