package revolut.backendtest.service;

public class NotEnoughMoneyException extends Exception {

  NotEnoughMoneyException(String message) {
    super(message);
  }
}
