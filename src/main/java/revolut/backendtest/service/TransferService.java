package revolut.backendtest.service;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static revolut.backendtest.persistence.jooq.codegen.Tables.TRANSFERS;
import static revolut.backendtest.persistence.jooq.codegen.tables.Accounts.ACCOUNTS;
import static revolut.backendtest.service.Mappers.toModel;
import static revolut.backendtest.service.Preconditions.checkScale;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.jooq.DSLContext;
import org.jooq.Record2;
import org.jooq.exception.DataAccessException;
import revolut.backendtest.model.AccountId;
import revolut.backendtest.model.Transfer;
import revolut.backendtest.model.TransferId;
import revolut.backendtest.persistence.jooq.JooqContextProvider;
import revolut.backendtest.persistence.jooq.codegen.tables.records.TransfersRecord;

public class TransferService {

  private final JooqContextProvider contextProvider;

  @Inject
  public TransferService(JooqContextProvider contextProvider) {
    this.contextProvider = contextProvider;
  }

  private DSLContext context() {
    return contextProvider.context();
  }

  public Optional<Transfer> get(TransferId id) {
    return context()
        .selectFrom(TRANSFERS)
        .where(TRANSFERS.TRANSFER_ID.eq(id.value))
        .fetchOptional()
        .map(Mappers::toModel);
  }

  public ImmutableList<Transfer> getAll() {
    return context()
        .selectFrom(TRANSFERS)
        .fetch()
        .stream()
        .map(Mappers::toModel)
        .collect(toImmutableList());
  }

  // Using a lot of checked exceptions here for simplicity only.
  // It will be better to collapse all those service exceptions into single one
  // and use some enum code to distinguish them. Or just use Try/Either approach instead.
  public Transfer makeTransfer(
      AccountId from,
      AccountId to,
      BigDecimal amount
  ) throws AccountNotFoundException, NotEnoughMoneyException, IllegalAmountException, SelfTransferException {
    checkNoSelfTransfer(from, to);
    checkPositive(amount);
    checkScale(amount);

    try {
      return tryMakeTransfer(from, to, amount);
    } catch (DataAccessException dae) {
      rethrowOnCause(dae, AccountNotFoundException.class);
      rethrowOnCause(dae, NotEnoughMoneyException.class);
      rethrowOnCause(dae, IllegalAmountException.class);
      throw dae;
    }
  }

  private static void checkNoSelfTransfer(
      AccountId from,
      AccountId to
  ) throws SelfTransferException {
    if (from.equals(to)) {
      throw new SelfTransferException("no self transfers allowed");
    }
  }

  private static void checkPositive(BigDecimal amount) throws IllegalAmountException {
    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalAmountException("Cannot transfer non-positive amount");
    }
  }

  private static <X extends Exception> void rethrowOnCause(
      DataAccessException dae,
      Class<X> causeClass
  ) throws X {
    X cause = dae.getCause(causeClass);
    if (cause != null) {
      throw cause;
    }
  }

  private Transfer tryMakeTransfer(AccountId from, AccountId to, BigDecimal amount) {
    return context().transactionResult(
        cfg -> {
          DSLContext context = cfg.dsl();

          Map<Long, Record2<Long, BigDecimal>> accounts = context
              .select(ACCOUNTS.ACCOUNT_ID, ACCOUNTS.BALANCE)
              .from(ACCOUNTS)
              .where(ACCOUNTS.ACCOUNT_ID.in(from.value, to.value))
              .forUpdate()
              .fetchMap(ACCOUNTS.ACCOUNT_ID);

          BigDecimal fromBalance = Optional.ofNullable(accounts.get(from.value))
              .map(Record2::component2)
              .orElseThrow(accountNotFound(from));
          if (fromBalance.compareTo(amount) < 0) {
            throw new NotEnoughMoneyException("Not enough money to transfer");
          }
          //noinspection ResultOfMethodCallIgnored
          Optional.ofNullable(accounts.get(to.value)).orElseThrow(accountNotFound(to));

          context
              .update(ACCOUNTS)
              .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.minus(amount))
              .where(ACCOUNTS.ACCOUNT_ID.eq(from.value))
              .execute();

          context
              .update(ACCOUNTS)
              .set(ACCOUNTS.BALANCE, ACCOUNTS.BALANCE.plus(amount))
              .where(ACCOUNTS.ACCOUNT_ID.eq(to.value))
              .execute();

          TransfersRecord transfer = context
              .insertInto(
                  TRANSFERS,
                  TRANSFERS.FROM_ACCOUNT_ID,
                  TRANSFERS.TO_ACCOUNT_ID,
                  TRANSFERS.AMOUNT
              )
              .values(from.value, to.value, amount)
              .returning()
              .fetchOne();

          return toModel(transfer);
        }
    );
  }

  private static Supplier<AccountNotFoundException> accountNotFound(AccountId from) {
    return () -> new AccountNotFoundException("Cannot find account with id=" + from.value);
  }
}
