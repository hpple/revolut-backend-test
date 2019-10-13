package revolut.backendtest.service;

import java.math.BigDecimal;

final class Preconditions {

  static void checkScale(BigDecimal amount) throws IllegalAmountException {
    if (amount.scale() > 2) {
      throw new IllegalAmountException(
          "Number of digits to the right of the decimal point should be <= 2"
      );
    }
  }
}
