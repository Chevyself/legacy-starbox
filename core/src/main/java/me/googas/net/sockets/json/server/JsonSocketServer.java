package me.googas.net.sockets.json.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.NonNull;
import me.googas.net.api.Server;
import me.googas.net.api.auth.Authenticator;
import me.googas.net.api.exception.MessengerListenFailException;
import me.googas.net.api.messages.Message;
import me.googas.net.api.messages.StarboxRequest;
import me.googas.net.sockets.json.JsonReceptor;
import me.googas.net.sockets.json.adapters.MessageDeserializer;
import me.googas.net.sockets.json.reflect.ReflectJsonReceptor;

/** An implementation for socket servers for guido. */
public class JsonSocketServer extends Thread implements Server<JsonClientThread> {

  /** The actual server socket. */
  @NonNull private final ServerSocket server;

  /** The set of clients that are connected to the server. */
  @NonNull @Getter private final Set<JsonClientThread> clients = new HashSet<>();

  /** The receptors to accept requests. */
  @NonNull @Getter private final Set<JsonReceptor> receptors;

  /** To handle exceptions thrown. */
  @NonNull @Getter private final Consumer<Throwable> throwableHandler;
  /** the gson instance for the server and clients deserialization . */
  @NonNull @Getter private final Gson gson;
  /** The time to timeout requests. */
  @Getter private final long timeout;
  /** The authenticator for the requests. */
  private Authenticator<JsonClientThread> authenticator;

  /**
   * Create the server.
   *
   * @param server the socket server which will send rand receive messages
   * @param receptors the receptors to handle requests
   * @param throwableHandler the handler for exceptions
   * @param gson the gson to serialize and deserialize objects
   * @param timeout the maximum timeout for messages in millis
   * @param authenticator the authentication methods that clients must complete to connect in the
   *     server
   */
  protected JsonSocketServer(
      @NonNull ServerSocket server,
      @NonNull Set<JsonReceptor> receptors,
      @NonNull Consumer<Throwable> throwableHandler,
      @NonNull Gson gson,
      long timeout,
      Authenticator<JsonClientThread> authenticator) {
    this.server = server;
    this.receptors = receptors;
    this.throwableHandler = throwableHandler;
    this.gson = gson;
    this.timeout = timeout;
    this.authenticator = authenticator;
  }

  /**
   * Creates the guido socket server
   *
   * @param port the port to which the server will be listening to
   * @param receptors the receptors to accept requests
   * @param throwableHandler to handle exceptions thrown
   * @param authenticator the authenticator for requests
   * @param gson the gson instance for the server and clients deserialization
   * @param timeout the time too timeout requests
   * @throws IOException if the port is already in use
   */
  @Deprecated
  public JsonSocketServer(
      int port,
      @NonNull Set<JsonReceptor> receptors,
      @NonNull Consumer<Throwable> throwableHandler,
      Authenticator<JsonClientThread> authenticator,
      @NonNull Gson gson,
      long timeout)
      throws IOException {
    this.server = new ServerSocket(port);
    this.receptors = receptors;
    this.throwableHandler = throwableHandler;
    this.authenticator = authenticator;
    this.gson = gson;
    this.timeout = timeout;
  }

  /**
   * Creates the guido socket server with the default receptors and providers
   *
   * @param port the port to which the server will be listening to
   * @param throwableHandler to handle exceptions thrown
   * @param authenticator the authenticator for requests
   * @param gson the gson instance for the server and clients deserialization
   * @param timeout the time too timeout requests
   * @throws IOException if the port is already in use
   */
  @Deprecated
  public JsonSocketServer(
      int port,
      @NonNull Consumer<Throwable> throwableHandler,
      Authenticator<JsonClientThread> authenticator,
      @NonNull Gson gson,
      long timeout)
      throws IOException {
    this(port, new HashSet<>(), throwableHandler, authenticator, gson, timeout);
  }

  /**
   * Start a builder for a server.
   *
   * @param port the port to which the server will listen to
   * @return the builder instance
   */
  @NonNull
  public static ServerBuilder listen(int port) {
    return new ServerBuilder(port);
  }

  /**
   * Remove a client from the set of clients.
   *
   * @param client the client to remove from the set
   */
  public void remove(@NonNull JsonClientThread client) {
    this.clients.remove(client);
    this.onRemove(client);
  }

  /**
   * Called when a client is already disconnected and {@link #remove(JsonClientThread)} was called.
   *
   * @param client the client that was removed
   */
  protected void onRemove(@NonNull JsonClientThread client) {
    System.out.println(client + " got disconnected");
  }

  /**
   * Disconnects a client from the server.
   *
   * @param client the client that disconnected
   */
  public void disconnect(@NonNull JsonClientThread client) {
    client.close();
    this.remove(client);
  }

  @Override
  public Optional<Authenticator<JsonClientThread>> getAuthenticator() {
    return Optional.ofNullable(this.authenticator);
  }

  @Override
  public boolean hasAuthentication() {
    return this.authenticator != null;
  }

  @Override
  public @NonNull JsonSocketServer setAuthenticator(
      @NonNull Authenticator<JsonClientThread> authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  @Override
  public void close() throws IOException {
    List<JsonClientThread> copy = new ArrayList<>(this.getClients());
    for (JsonClientThread client : copy) {
      this.disconnect(client);
    }
    this.server.close();
    this.receptors.clear();
  }

  @Override
  public void run() {
    while (true) {
      try {
        Socket socket = this.server.accept();
        JsonClientThread client = new JsonClientThread(socket, this, this.timeout);
        client.start();
        this.clients.add(client);
        this.onConnection(client);
      } catch (IOException e) {
        this.throwableHandler.accept(e);
        break;
      }
    }
  }

  @Override
  public <T> void sendRequest(
      @NonNull StarboxRequest<T> request, BiConsumer<JsonClientThread, Optional<T>> consumer) {
    this.clients.forEach(
        client -> {
          try {
            consumer.accept(client, client.sendRequest(request));
          } catch (MessengerListenFailException e) {
            this.throwableHandler.accept(e);
          }
        });
  }

  @Override
  @NonNull
  public <T> Map<JsonClientThread, Optional<T>> sendRequest(@NonNull StarboxRequest<T> request) {
    Map<JsonClientThread, Optional<T>> responses = new HashMap<>();
    this.clients.forEach(
        client -> {
          try {
            responses.put(client, client.sendRequest(request));
          } catch (MessengerListenFailException e) {
            this.throwableHandler.accept(e);
          }
        });
    return responses;
  }

  /**
   * Called when a client gets connected to the server.
   *
   * @param client the client connecting to the server
   */
  protected void onConnection(@NonNull JsonClientThread client) {
    System.out.println(client + " got connected");
  }

  public void addReceptors(@NonNull Object... objects) {
    for (Object object : objects) {
      this.receptors.addAll(ReflectJsonReceptor.getReceptors(object));
    }
  }

  /** This class is used to create instances of servers in a neat way. */
  public static class ServerBuilder {

    @NonNull private final Set<JsonReceptor> receptors;
    private final int port;
    @NonNull private GsonBuilder gson;
    @NonNull private Consumer<Throwable> handler;
    private long timeout;
    private Authenticator<JsonClientThread> authenticator;

    /**
     * Create the builder.
     *
     * @param port the port to which the server will listen to
     */
    private ServerBuilder(int port) {
      this.port = port;
      this.receptors = new HashSet<>();
      this.gson = new GsonBuilder().registerTypeAdapter(Message.class, new MessageDeserializer());
      this.handler = Throwable::printStackTrace;
      this.timeout = 1000;
    }

    /**
     * Set the exception handler that the client may use.
     *
     * @param handler the new exception handler
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder handle(@NonNull Consumer<Throwable> handler) {
      this.handler = handler;
      return this;
    }

    /**
     * Set the maximum time that the server will tolerate.
     *
     * @param timeout the new maximum time in millis
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder maxWait(long timeout) {
      this.timeout = timeout;
      return this;
    }

    /**
     * Sets the authentication method which clients may use.
     *
     * @param authenticator the new authentication method
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder auth(Authenticator<JsonClientThread> authenticator) {
      this.authenticator = authenticator;
      return this;
    }

    /**
     * Adds the parsed receptors from the given object. This will get the receptors from the object
     * using {@link ReflectJsonReceptor#getReceptors(Object)} and add them to the set
     *
     * @param objects the objects to add as receptors
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder addReceptors(@NonNull Object... objects) {
      for (Object object : objects) {
        this.addReceptors(ReflectJsonReceptor.getReceptors(object));
      }
      return this;
    }

    /**
     * Adds all the given receptors.
     *
     * @param receptors the receptors to add
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder addReceptors(@NonNull JsonReceptor... receptors) {
      this.receptors.addAll(Arrays.asList(receptors));
      return this;
    }

    /**
     * Adds all the given receptors.
     *
     * @param receptors the receptors to add
     * @return this same builder instance
     */
    @NonNull
    public ServerBuilder addReceptors(@NonNull Collection<JsonReceptor> receptors) {
      this.receptors.addAll(receptors);
      return this;
    }

    /**
     * Starts the server.
     *
     * @return the server instance
     * @throws IOException if the server could not be created
     */
    @NonNull
    public JsonSocketServer start() throws IOException {
      JsonSocketServer server =
          new JsonSocketServer(
              new ServerSocket(this.port),
              this.receptors,
              this.handler,
              this.gson.create(),
              this.timeout,
              this.authenticator);
      server.start();
      return server;
    }

    /**
     * Set the instance of {@link GsonBuilder}.
     *
     * @param gson the new builder
     * @return this same instance
     * @see #getGsonBuilder()
     */
    @NonNull
    public ServerBuilder setGson(@NonNull GsonBuilder gson) {
      this.gson = gson;
      return this;
    }

    /**
     * Get the instance of {@link GsonBuilder} that will create the {@link Gson} of the server to
     * read messages.
     *
     * @return the builder
     */
    @NonNull
    public GsonBuilder getGsonBuilder() {
      return this.gson;
    }
  }
}
