package me.googas.net.sockets.json.exception;

/** Thrown when a given argument is illegal. */
public class JsonIllegalArgumentException extends JsonExternalCommunicationException {

  /**
   * Constructs a new exception with the specified detail message. The cause is not initialized, and
   * may subsequently be initialized by a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   *     {@link #getMessage()} method.
   */
  public JsonIllegalArgumentException(String message) {
    super(message);
  }
}
