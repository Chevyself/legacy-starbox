package me.googas.net.api;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import lombok.NonNull;
import me.googas.net.api.exception.MessengerListenFailException;
import me.googas.net.api.messages.Message;
import me.googas.net.api.messages.StarboxRequest;

/** This object is used to give and receive {@link Message}. */
public interface Messenger {
  /**
   * Listens for incoming messages.
   *
   * @throws MessengerListenFailException if the messenger fails to listen to new messages
   */
  void listen() throws MessengerListenFailException;

  /** Closes the messenger. */
  void close();

  /**
   * Sends a request to this messenger asynchronously.
   *
   * @param request the request that was send and must be processed by this messenger
   * @param consumer the consumer to provide the object when the request gets a response
   * @param <T> the type of object that the request expects
   */
  @Deprecated
  default <T> void sendRequest(
      @NonNull StarboxRequest<T> request, @NonNull Consumer<Optional<T>> consumer) {
    this.send(request)
        .thenAccept(
            (tObj) -> {
              consumer.accept(Optional.ofNullable(tObj));
            });
  }

  /**
   * Sends a request to get the requested object.
   *
   * @param request the request that was send and must provided the object
   * @param <T> the type of the object
   * @return the provided object wrapped in a {@link Optional} instance
   * @throws MessengerListenFailException if the request times out
   */
  @Deprecated
  default <T> @NonNull Optional<T> sendRequest(@NonNull StarboxRequest<T> request)
      throws MessengerListenFailException {
    try {
      T tObj = this.send(request).get();
      return Optional.ofNullable(tObj);
    } catch (ExecutionException e) {
      throw new MessengerListenFailException("Failed execution", e);
    } catch (InterruptedException e) {
      throw new MessengerListenFailException("Interrupted", e);
    }
  }

  <T> @NonNull CompletableFuture<T> send(@NonNull StarboxRequest<T> request);
}
