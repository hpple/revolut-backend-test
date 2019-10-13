package revolut.backendtest.service;

public class AccountNotFoundException extends Exception {

  AccountNotFoundException(String message) {
    super(message);
  }
}
