package revolut.backendtest.api;

public class BadRequest extends RuntimeException {

  public BadRequest(String message) {
    super(message);
  }

  public BadRequest(String message, Exception ex) {
    super(message, ex);
  }

}
