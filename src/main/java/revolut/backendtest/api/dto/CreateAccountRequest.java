package revolut.backendtest.api.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import java.math.BigDecimal;

public final class CreateAccountRequest {

  public BigDecimal balance;

  @JsonCreator
  public CreateAccountRequest(BigDecimal balance) {
    this.balance = balance;
  }
}
