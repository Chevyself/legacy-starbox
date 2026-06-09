package me.googas.net.sockets.json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import lombok.NonNull;
import me.googas.net.api.Error;
import me.googas.net.api.Messenger;
import me.googas.net.api.exception.MessengerListenFailException;
import me.googas.net.api.messages.AwaitingRequest;
import me.googas.net.api.messages.Message;
import me.googas.net.api.messages.Request;
import me.googas.net.api.messages.Response;
import me.googas.net.api.messages.StarboxRequest;
import me.googas.net.sockets.json.exception.JsonCommunicationException;
import me.googas.net.sockets.json.exception.JsonExternalCommunicationException;
import me.googas.net.sockets.json.exception.JsonInternalCommunicationException;
import me.googas.net.sockets.json.server.JsonClientThread;

/** A {@link Messenger} that works with json messages. */
public interface JsonMessenger extends Messenger, Runnable {

  /**
   * Prints a line in the output stream.
   *
   * @param line the line to print
   */
  default void printLine(@NonNull String line) {
    this.getOutput().println(line + "\n---");
  }

  /**
   * Get the awaiting request matching the uuid.
   *
   * @param uuid the uuid to match
   * @return the matched request
   */
  @NonNull
  default Optional<AwaitingRequest<?>> getRequest(@NonNull UUID uuid) {
    return this.getRequests().keySet().stream()
        .filter(awaiting -> awaiting.getRequest().getId().equals(uuid))
        .findFirst();
  }

  /**
   * Get the matching receptor for a request.
   *
   * @param request the request that needs a receptor
   * @return the receptor if found else null
   */
  @NonNull
  default Optional<JsonReceptor> getReceptor(@NonNull Request request) {
    return this.getReceptor(request.getMethod());
  }

  /**
   * Get a receptor by its method.
   *
   * @param method the method to match
   * @return the possible receptor in an optional wrapper
   */
  @NonNull
  default Optional<JsonReceptor> getReceptor(@NonNull String method) {
    return this.getReceptors().stream()
        .filter(receptor -> receptor.getRequestMethod().equalsIgnoreCase(method))
        .findFirst();
  }

  /**
   * Accepts a request.
   *
   * @param request the request to be accepted
   */
  default void acceptRequest(@NonNull ReceivedJsonRequest request) {
    CompletableFuture.runAsync(
        () -> {
          Optional<JsonReceptor> optional = this.getReceptor(request);
          Response<?> response;
          if (optional.isPresent()) {
            try {
              JsonReceptor receptor = optional.get();
              response =
                  new Response<>(request.getId(), receptor.execute(this, request, this.getGson()));
              response.setError(false);
            } catch (JsonExternalCommunicationException e) {
              response = new Response<>(request.getId(), new Error(e.getMessage()));
            } catch (JsonInternalCommunicationException e) {
              response =
                  new Response<>(request.getId(), new Error("Internal Error: " + e.getMessage()));
              this.getThrowableHandler().accept(e);
            }
          } else {
            response = new Response<>(request.getId(), null);
            response.setError(false);
          }
          this.printLine(this.getGson().toJson(response));
        });
  }

  /**
   * Send a request.
   *
   * <p>This method will give you the option to change what to do in case of an exception such as a
   * timeout
   *
   * @param request the request to send
   * @param consumer the method to execute when the result is given
   * @param exception the method to execute in case an exception is thrown
   * @param <T> the type of object requested
   */
  @Deprecated
  default <T> void sendRequest(
      @NonNull StarboxRequest<T> request,
      @NonNull Consumer<Optional<T>> consumer,
      @NonNull Consumer<Throwable> exception) {
    this.getRequests()
        .put(
            new AwaitingRequest<>(request, request.getClazz(), consumer, exception),
            System.currentTimeMillis());
    this.printLine(this.getGson().toJson(request));
  }

  /**
   * Set whether this messenger is closed.
   *
   * @param bol the new value of closed
   */
  void setClosed(boolean bol);

  /**
   * Set the millis of the last message sent.
   *
   * @param millis the millis of the last message sent
   */
  void setLastMessage(long millis);

  /**
   * Get the output line to send messages.
   *
   * @return the output line to send messages
   */
  @NonNull
  PrintWriter getOutput();

  /**
   * Get the input line to receive messages.
   *
   * @return the input line
   */
  @NonNull
  BufferedReader getInput();

  /**
   * Get the receptors that the messenger is capable of using.
   *
   * @return the collection of receptors
   */
  @NonNull
  Collection<JsonReceptor> getReceptors();

  /**
   * Get when request may timeout.
   *
   * @return the time to timeout in millis
   */
  long getTimeout();

  /**
   * Get whether this messenger is closed.
   *
   * @return true if the messenger is closed
   */
  boolean isClosed();

  /**
   * Get the request that this messenger has sent and the time when they were sent.
   *
   * @return the request that this messenger has sent
   */
  @NonNull
  Map<AwaitingRequest<?>, Long> getRequests();

  /**
   * Get the gson instance that this messenger may use.
   *
   * @return the gson instance
   */
  @NonNull
  Gson getGson();

  /**
   * Get the socket that this messenger is on.
   *
   * @return the messenger
   */
  @NonNull
  Socket getSocket();

  /**
   * Get the throwable handler that this messenger uses in case of a wrong request.
   *
   * @return the throwable handler
   */
  @NonNull
  Consumer<Throwable> getThrowableHandler();

  /**
   * Get the string builder that the messenger can use.
   *
   * @return the string builder
   */
  @NonNull
  StringBuilder getBuilder();

  @Override
  default void run() {
    while (true) {
      try {
        if (this.isClosed()) {
          break;
        } else {
          this.listen();
        }
      } catch (MessengerListenFailException e) {
        this.getThrowableHandler().accept(e);
        this.close();
        break;
      }
    }
  }

  @Override
  default void listen() throws MessengerListenFailException {
    try {
      StringBuilder builder = this.getBuilder();
      builder.setLength(0);
      String line;
      boolean closed = false;
      while ((line = this.getInput().readLine()) != null
          || (closed = this.getInput().read() == -1)) {
        if (closed) break;
        if (line.startsWith("Invalid Message:")) {
          this.getThrowableHandler().accept(new JsonCommunicationException(line));
          builder.setLength(0);
          break;
        }
        if (line.equalsIgnoreCase("---") || !this.getInput().ready()) {
          break;
        } else {
          builder.append(line).append("\n");
        }
      }
      this.setLastMessage(System.currentTimeMillis());
      if (builder.length() != 0) {
        Gson gson = this.getGson();
        String json = builder.toString();
        try {
          Message message = gson.fromJson(json, Message.class);
          if (message instanceof ReceivedJsonRequest) {
            this.acceptRequest((ReceivedJsonRequest) message);
          } else if (message instanceof Response) {
            Optional<AwaitingRequest<?>> optional = this.getRequest(message.getId());
            JsonObject object = gson.fromJson(json, JsonObject.class);
            if (optional.isPresent()) {
              AwaitingRequest<?> awaitingRequest = optional.get();
              if (((Response<?>) message).isError()) {
                if (!object.get("object").isJsonNull()) {
                  awaitingRequest.completeExceptionally(
                      new JsonInternalCommunicationException(
                          gson.fromJson(object.get("object"), Error.class).getCause()));
                } else {
                  awaitingRequest.complete(null);
                }
              } else {
                if (object.get("object") != null && !object.get("object").isJsonNull()) {
                  awaitingRequest.completeFromJson(gson, object.get("object"));
                } else {
                  awaitingRequest.complete(null);
                }
              }
              this.getRequests().remove(awaitingRequest);
            }
          }
        } catch (RuntimeException e) {
          if (this instanceof JsonClientThread) {
            this.printLine("Invalid Message: " + e.getMessage());
          } else {
            this.getThrowableHandler().accept(e);
          }
        }
      }
    } catch (IOException e) {
      if (!this.isClosed()) throw new MessengerListenFailException(null, e);
    }
  }

  @Override
  default <T> @NonNull CompletableFuture<T> send(@NonNull StarboxRequest<T> request) {
    CompletableFuture<T> future = new CompletableFuture<>();
    AwaitingRequest<T> awaitingRequest = new AwaitingRequest<>(request, request.getClazz(), future);
    this.getRequests().put(awaitingRequest, System.currentTimeMillis());
    this.printLine(this.getGson().toJson(request));
    JsonScheduler.INSTANCE.schedule(
        () ->
            future.completeExceptionally(
                new MessengerListenFailException(
                    "The request " + request + " has timed out after " + this.getTimeout() + "ms")),
        this.getTimeout(),
        TimeUnit.MILLISECONDS);
    future.whenComplete((result, ex) -> this.getRequests().remove(awaitingRequest));
    return future;
  }
}
