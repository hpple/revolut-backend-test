package revolut.backendtest.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.collectingAndThen;
import static revolut.backendtest.persistence.jooq.codegen.Tables.ACCOUNTS;
import static revolut.backendtest.persistence.jooq.codegen.tables.Transfers.TRANSFERS;
import static revolut.backendtest.service.Preconditions.checkScale;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Optional;
import org.jooq.DSLContext;
import org.jooq.Result;
import revolut.backendtest.model.Account;
import revolut.backendtest.model.AccountId;
import revolut.backendtest.model.AccountTransfers;
import revolut.backendtest.persistence.jooq.JooqContextProvider;
import revolut.backendtest.persistence.jooq.codegen.tables.records.AccountsRecord;
import revolut.backendtest.persistence.jooq.codegen.tables.records.TransfersRecord;

public class AccountService {

  private static final Optional<AccountTransfers> EmptyTransfers = Optional.of(
      AccountTransfers.Empty
  );

  private final JooqContextProvider contextProvider;

  @Inject
  public AccountService(JooqContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  private DSLContext context() {
    return contextProvider.context();
  }

  public Optional<Account> get(AccountId id) {
    return context()
        .selectFrom(ACCOUNTS)
        .where(ACCOUNTS.ACCOUNT_ID.eq(id.value))
        .fetchOptional()
        .map(Mappers::toModel);
  }

  public ImmutableList<Account> getAll() {
    return context()
        .selectFrom(ACCOUNTS)
        .fetch()
        .stream()
        .map(Mappers::toModel)
        .collect(toImmutableList());
  }

  public Account create(BigDecimal initialBalance) throws IllegalAmountException {
    checkNonNegative(initialBalance);
    checkScale(initialBalance);

    return context().transactionResult(
        cfg -> {
          AccountsRecord record = cfg.dsl()
              .insertInto(ACCOUNTS)
              .set(ACCOUNTS.BALANCE, initialBalance)
              .returning()
              .fetchOne();

          return Mappers.toModel(record);
        }
    );
  }

  private static void checkNonNegative(BigDecimal balance) throws IllegalAmountException {
    if (balance.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalAmountException("Amount should be non-negative");
    }
  }

  public Optional<AccountTransfers> getTransfers(AccountId id) {
    Result<TransfersRecord> result = context()
        .select(TRANSFERS.fields())
        .from(ACCOUNTS).leftJoin(TRANSFERS)
        .on(TRANSFERS.FROM_ACCOUNT_ID.eq(id.value).or(TRANSFERS.TO_ACCOUNT_ID.eq(id.value)))
        .where(ACCOUNTS.ACCOUNT_ID.eq(id.value))
        .orderBy(TRANSFERS.TRANSFERED_AT.desc())
        .fetchInto(TRANSFERS);

    if (result.isEmpty()) {
      return Optional.empty();
    }

    if (result.get(0).getTransferId() == null) {
      return EmptyTransfers;
    }

    return Optional.of(
        result.stream()
            .map(Mappers::toModel)
            .collect(collectingAndThen(
                toImmutableList(),
                AccountTransfers::new
            ))
    );
  }
}
