package revolut.backendtest.api.spark;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.function.Supplier;
import revolut.backendtest.api.BadRequest;
import revolut.backendtest.api.NotFound;
import revolut.backendtest.api.dto.CreateAccountRequest;
import revolut.backendtest.model.Account;
import revolut.backendtest.model.AccountId;
import revolut.backendtest.model.Transfer;
import revolut.backendtest.service.AccountService;
import revolut.backendtest.service.IllegalAmountException;
import spark.Request;
import spark.Response;

public class AccountController {

  private final RequestTransformer transformer;
  private final AccountService accounts;

  @Inject
  public AccountController(
      RequestTransformer transformer,
      AccountService accounts
  ) {
    this.transformer = transformer;
    this.accounts = accounts;
  }

  Account createAccount(Request request, Response response) {
    CreateAccountRequest rq = transformer.transform(request, CreateAccountRequest.class);
    if (rq.balance == null) {
      throw new BadRequest("Missing initial amount");
    }

    try {
      return accounts.create(rq.balance);
    } catch (IllegalAmountException ex) {
      throw new BadRequest(ex.getMessage(), ex);
    }
  }

  ImmutableList<Account> getAccounts(Request request, Response response) {
    return accounts.getAll();
  }

  Account getAccount(Request request, Response response) {
    AccountId accountId = getAccountId(request);
    return accounts.get(accountId).orElseThrow(notFound(accountId));
  }

  private static AccountId getAccountId(Request request) {
    return parseAccountId(request.params(":id"));
  }

  private static AccountId parseAccountId(String id) {
    try {
      return new AccountId(Long.parseLong(id));
    } catch (NumberFormatException ex) {
      throw new BadRequest("Incorrect account id: " + id, ex);

    }
  }

  private static Supplier<NotFound> notFound(AccountId accountId) {
    return () -> new NotFound("Cannot find account with id=" + accountId.value);
  }

  ImmutableList<Transfer> getAccountTransfers(Request request, Response response) {
    AccountId accountId = getAccountId(request);
    return accounts.getTransfers(accountId)
        .orElseThrow(notFound(accountId))
        .transfers;
  }
}
