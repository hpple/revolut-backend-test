package revolut.backendtest.api.dto;

public class ErrorResponse {

  public final String message;

  public ErrorResponse(String message) {
    this.message = message;
  }
}
