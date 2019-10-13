package revolut.backendtest.model;

import java.math.BigDecimal;
import java.time.Instant;

public final class Transfer {
  public final TransferId id;
  public final AccountId from;
  public final AccountId to;
  public final Instant at;
  public final BigDecimal amount;

  public Transfer(
      TransferId id,
      AccountId from,
      AccountId to,
      Instant at,
      BigDecimal amount
  ) {
    this.id = id;
    this.from = from;
    this.to = to;
    this.at = at;
    this.amount = amount;
  }
}
