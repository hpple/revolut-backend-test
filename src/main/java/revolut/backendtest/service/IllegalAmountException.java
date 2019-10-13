package revolut.backendtest.service;

public class IllegalAmountException extends Exception {

  public IllegalAmountException(String message) {
    super(message);
  }
}
