package revolut.backendtest.service;

public class SelfTransferException extends Exception {

  public SelfTransferException(String message) {
    super(message);
  }
}
