package revolut.backendtest.api;

public class NotFound extends RuntimeException {

  public NotFound(String message) {
    super(message);
  }

  public NotFound(String message, Exception ex) {
    super(message, ex);
  }
}
