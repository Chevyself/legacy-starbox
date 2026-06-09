package me.googas.net.api.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.NonNull;
import me.googas.net.api.Messenger;
import me.googas.net.api.Server;
import me.googas.net.api.exception.MessengerListenFailException;

/**
 * Builds a request to be used in a {@link Messenger}.
 *
 * @param <T> the type of object that the request must return
 */
public class RequestBuilder<T> {

  @NonNull private final Class<T> clazz;
  @NonNull private final Map<String, Object> parameters;
  @NonNull private String method;

  /**
   * Create the request.
   *
   * @param clazz the clazz of the object to return
   * @param method the method that must match a receptor
   * @param parameters the parameters for the receptor
   */
  public RequestBuilder(
      @NonNull Class<T> clazz, @NonNull String method, @NonNull Map<String, Object> parameters) {
    this.clazz = clazz;
    this.method = method;
    this.parameters = parameters;
  }

  /**
   * Create the request.
   *
   * @param clazz the clazz of the object to return
   * @param method the method that must match a receptor
   */
  public RequestBuilder(@NonNull Class<T> clazz, @NonNull String method) {
    this(clazz, method, new HashMap<>());
  }

  /**
   * Create the request.
   *
   * @param clazz the clazz of the object to return
   */
  public RequestBuilder(@NonNull Class<T> clazz) {
    this(clazz, "none", new HashMap<>());
  }

  /**
   * Put a parameter for the request.
   *
   * @param key the key of the parameter
   * @param value the value of the parameter
   * @return this same instance
   */
  @NonNull
  public RequestBuilder<T> put(@NonNull String key, @NonNull Object value) {
    this.parameters.put(key, value);
    return this;
  }

  /**
   * Put many parameters in the request.
   *
   * @param map the map to get all the keys and values to put
   * @return this same instance
   */
  @NonNull
  public RequestBuilder<T> putAll(@NonNull Map<String, ?> map) {
    this.parameters.putAll(map);
    return this;
  }

  /**
   * Build the request.
   *
   * @return the built request
   */
  @NonNull
  public StarboxRequest<T> build() {
    return new StarboxRequest<>(this.clazz, this.method, this.parameters);
  }

  /**
   * Send the request sync.
   *
   * @param messenger the messenger to send the request
   * @return an {@link Optional} instance holding the requested object
   * @throws MessengerListenFailException if the request fails to be completed
   */
  @Deprecated
  public @NonNull Optional<T> send(Messenger messenger) throws MessengerListenFailException {
    return messenger == null ? Optional.empty() : messenger.sendRequest(this.build());
  }

  /**
   * Send the request async.
   *
   * @param messenger the messenger to send the request
   * @param consumer the consumer holding the {@link Optional} instance with the requested object
   */
  @Deprecated
  public void send(Messenger messenger, @NonNull Consumer<Optional<T>> consumer) {
    if (messenger == null) {
      consumer.accept(Optional.empty());
      return;
    }
    messenger.sendRequest(this.build(), consumer);
  }

  /**
   * Send the request async.
   *
   * @param messenger the messenger to send the request
   */
  @Deprecated
  public void queue(Messenger messenger) {
    this.send(messenger, (optional) -> {});
  }

  public @NonNull CompletableFuture<T> future(Messenger messenger) {
    return messenger == null ? CompletableFuture.completedFuture(null) : messenger.send(this.build());
  }

  /**
   * Send the request using a server sync.
   *
   * @param server the server to send the request
   * @param <M> the type of messenger of the server
   * @return a map holding each messenger and its response
   */
  @Deprecated
  public <M extends Messenger> Map<M, Optional<T>> send(Server<M> server) {
    return server == null ? new HashMap<>() : server.sendRequest(this.build());
  }

  /**
   * Send the request using a server async.
   *
   * @param server the server to send the request
   * @param consumer a bi-consumer for each messenger and its response
   * @param <M> the type of messenger of the server
   */
  @Deprecated
  public <M extends Messenger> void send(
      Server<M> server, @NonNull BiConsumer<M, Optional<T>> consumer) {
    if (server != null) {
      server.sendRequest(this.build(), consumer);
    }
  }

  /**
   * Send the request using a server async.
   *
   * @param server the server to send the request
   * @param <M> the type of messenger of the server
   */
  @Deprecated
  public <M extends Messenger> void queue(@NonNull Server<M> server) {
    this.send(server, (messenger, optional) -> {});
  }

  public <M extends Messenger> CompletableFuture<Map<M, T>> future(Server<M> server) {
    if (server == null) return CompletableFuture.completedFuture(new HashMap<>());

    Map<M, CompletableFuture<T>> futures = new HashMap<>();
    for (M messenger : server.getClients()) {
      futures.put(messenger, messenger.send(this.build()));
    }

    return CompletableFuture.allOf(futures.values().toArray(new CompletableFuture[0]))
            .thenApply(v -> {
              Map<M, T> results = new HashMap<>();
              futures.forEach((messenger, future) -> results.put(messenger, future.join()));
              return results;
            });
  }

  /**
   * Set the method that the request must use.
   *
   * @param method the method of the request
   * @return this same instance
   */
  @NonNull
  public RequestBuilder<T> setMethod(@NonNull String method) {
    this.method = method;
    return this;
  }
}
