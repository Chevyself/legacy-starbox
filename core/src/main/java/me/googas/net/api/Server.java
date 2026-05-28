package me.googas.net.api;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import lombok.NonNull;
import me.googas.net.api.auth.Authenticator;
import me.googas.net.api.messages.StarboxRequest;
import me.googas.net.sockets.json.server.JsonClientThread;

/** This object represents the server {@link Messenger} connects to. */
public interface Server<M extends Messenger> {
  /**
   * Closes the server.
   *
   * @throws IOException some objects when closed can cause this exception
   */
  void close() throws IOException;

  /**
   * Whether clients need authentication to use.
   *
   * @return whether the server requires the client to be authenticated
   */
  default boolean hasAuthentication() {
    return this.getAuthenticator().isPresent();
  }

  /**
   * Send a request and accept the consumer for each client.
   *
   * @param request the request to send
   * @param consumer the consumer to accept
   * @param <T> the type of object requested
   */
  <T> void sendRequest(@NonNull StarboxRequest<T> request, BiConsumer<M, Optional<T>> consumer);

  /**
   * Send a request and get the response for each client.
   *
   * @param request the request to send
   * @param <T> the type of object request
   * @return the map of clients and its response
   */
  @NonNull
  <T> Map<M, Optional<T>> sendRequest(@NonNull StarboxRequest<T> request);

  /** Makes the server start listening. */
  void start();

  /**
   * Set the authenticator for request.
   *
   * @param authenticator the new authenticator
   * @return this same instance
   */
  @NonNull
  Server<M> setAuthenticator(@NonNull Authenticator<M> authenticator);

  /**
   * Get an {@link Optional} instance containing the authenticator for the client to send requests
   * to the server.
   *
   * @return the authenticator to send requests to the server
   */
  Optional<? extends Authenticator<JsonClientThread>> getAuthenticator();

  /**
   * Get the clients that are connected to the server.
   *
   * @return the set of clients connected to the server
   */
  @NonNull
  Collection<M> getClients();
}
