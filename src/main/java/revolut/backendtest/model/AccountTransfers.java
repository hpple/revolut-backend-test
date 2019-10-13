package revolut.backendtest.model;

import com.google.common.collect.ImmutableList;

public final class AccountTransfers {

  public static final AccountTransfers Empty = new AccountTransfers(ImmutableList.of());

  public final ImmutableList<Transfer> transfers;

  public AccountTransfers(ImmutableList<Transfer> transfers) {
    this.transfers = transfers;
  }
}
