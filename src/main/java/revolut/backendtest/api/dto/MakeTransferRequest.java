package revolut.backendtest.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;

public final class MakeTransferRequest {
  public final Long from;
  public final Long to;
  public final BigDecimal amount;

  @JsonCreator
  public MakeTransferRequest(Long from, Long to, BigDecimal amount) {
    this.from = from;
    this.to = to;
    this.amount = amount;
  }
}
