package revolut.backendtest.service;

import revolut.backendtest.model.Account;
import revolut.backendtest.model.AccountId;
import revolut.backendtest.model.Transfer;
import revolut.backendtest.model.TransferId;
import revolut.backendtest.persistence.jooq.codegen.tables.records.AccountsRecord;
import revolut.backendtest.persistence.jooq.codegen.tables.records.TransfersRecord;

final class Mappers {

  private Mappers() {
  }

  static Account toModel(AccountsRecord record) {
    return new Account(
        new AccountId(record.getAccountId()),
        record.getCreatedAt().toInstant(),
        record.getBalance()
    );
  }

  static Transfer toModel(TransfersRecord record) {
    return new Transfer(
        new TransferId(record.getTransferId()),
        new AccountId(record.getFromAccountId()),
        new AccountId(record.getToAccountId()),
        record.getTransferedAt().toInstant(),
        record.getAmount()
    );
  }
}
