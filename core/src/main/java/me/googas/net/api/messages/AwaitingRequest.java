package me.googas.net.api.messages;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import me.googas.net.api.Messenger;

/**
 * A request that is waiting for a {@link me.googas.net.api.messages.Response} in a {@link
 * Messenger}
 *
 * @param <T> the type of object that the request wanted
 */
public class AwaitingRequest<T> {

  /** The request waiting for the response. */
  @NonNull @Getter private final Request request;

  /** The class of the object requested. */
  @NonNull @Getter private final Class<T> clazz;

  /** The future awaiting this request completion */
  @NonNull private final CompletableFuture<T> future;

  /**
   * Create the awaiting request.
   *
   * @param request the request that is waiting for a response
   * @param clazz the class of the object that the request is waiting
   * @param consumer the consumer to execute when the response is received
   * @param exception the consumer in case of an exception
   */
  @Deprecated
  public AwaitingRequest(
      @NonNull Request request,
      @NonNull Class<T> clazz,
      @NonNull Consumer<Optional<T>> consumer,
      @NonNull Consumer<Throwable> exception) {
    this.request = request;
    this.clazz = clazz;
    this.future = composeFutureFromConsumers(consumer, exception);
  }

  public AwaitingRequest(
      @NonNull Request request, @NonNull Class<T> clazz, @NonNull CompletableFuture<T> future) {
    this.request = request;
    this.clazz = clazz;
    this.future = future;
  }

  @NonNull
  private static <T> CompletableFuture<T> composeFutureFromConsumers(
      @NonNull Consumer<Optional<T>> consumer, @NonNull Consumer<Throwable> exception) {
    return new CompletableFuture<T>()
        .handle(
            (tObj, throwable) -> {
              if (throwable != null) {
                exception.accept(throwable);
                return null;
              } else {
                consumer.accept(Optional.ofNullable(tObj));
                return tObj;
              }
            });
  }

  public void completeExceptionally(@NonNull Throwable ex) {
    this.future.completeExceptionally(ex);
  }

  public void complete(T tObj) {
    this.future.complete(tObj);
  }

  public void completeFromJson(@NonNull Gson gson, @NonNull JsonElement element) {
    this.future.complete(gson.fromJson(element, this.clazz));
  }

  /**
   * Create the awaiting request. With a simple print trace if something goes wrong
   *
   * @param request the request that is waiting for a response
   * @param clazz the class of the object that the request is waiting
   * @param consumer the consumer to execute when the response is received
   */
  @Deprecated
  public AwaitingRequest(
      @NonNull Request request, @NonNull Class<T> clazz, @NonNull Consumer<Optional<T>> consumer) {
    this(request, clazz, consumer, Throwable::printStackTrace);
  }

  @Override
  public String toString() {
    return this.request.toString();
  }
}
