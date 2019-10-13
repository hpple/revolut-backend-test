package revolut.backendtest.model;

import java.math.BigDecimal;
import java.time.Instant;

public final class Account {
  public final AccountId id;
  public final Instant createdAt;
  public final BigDecimal balance;

  public Account(
      AccountId id,
      Instant createdAt,
      BigDecimal balance
  ) {
    this.id = id;
    this.createdAt = createdAt;
    this.balance = balance;
  }
}
