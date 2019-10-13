package revolut.backendtest;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.spotify.futures.CompletableFutures.joinList;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.groups.Tuple.tuple;
import static revolut.backendtest.persistence.jooq.codegen.tables.Accounts.ACCOUNTS;
import static revolut.backendtest.persistence.jooq.codegen.tables.Transfers.TRANSFERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.assertj.core.groups.Tuple;
import org.eclipse.jetty.http.HttpStatus;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import revolut.backendtest.api.dto.CreateAccountRequest;
import revolut.backendtest.api.dto.MakeTransferRequest;

@TestInstance(Lifecycle.PER_METHOD)
class MoneyTransferApiTest {

  private ExecutorService executor;

  private MoneyTransferServer server;
  private Api api;

  @BeforeEach
  void setUp() throws Exception {
    MoneyTransferServer server = new MoneyTransferServer();
    server.start();

    this.executor = Executors.newCachedThreadPool();
    this.server = server;
    this.api = createApi();

    try (DSLContext jooq = DSL.using("jdbc:h2:mem:revolut;DB_CLOSE_DELAY=-1", "sa", "")) {
      jooq.deleteFrom(TRANSFERS).execute();
      jooq.deleteFrom(ACCOUNTS).execute();
    }
  }

  @AfterEach
  void tearDown() throws TimeoutException {
    if (executor != null) {
      executor.shutdownNow();
    }
    if (server != null) {
      server.stop();
    }
  }

  @Nested
  class Accounts {
    @Test
    @DisplayName("empty response when no accounts exist")
    void noAccounts() throws Exception {
      Response<List<AccountJson>> response = api.accounts().execute();

      assertOk(response);
      assertThat(response.body()).isEmpty();
    }

    @Test
    @DisplayName("404 when request account does not exist")
    void noAccount() throws Exception {
      Response<AccountJson> response = api.account("666").execute();

      assertNotFound(response);
    }

    @Test
    @DisplayName("bad request when account id is malformed")
    void badGetAccount() throws Exception {
      Response<AccountJson> response = api.account("bad").execute();

      assertBadRequest(response);
    }

    @ParameterizedTest(name = "initial balance = {0}")
    @CsvSource({"0", "0.01", "0.42", "1", "2.1", "3.14", "4444444.56"})
    @DisplayName("created account can be retrieved")
    void createAccount(BigDecimal balance) throws Exception {
      // Given
      CreateAccountRequest rq = new CreateAccountRequest(balance);
      Instant now = Instant.now();

      // When create
      Response<AccountJson> createRs = api.createAccount(rq).execute();
      AccountJson created = createRs.body();
      // Then
      assertOk(createRs);
      assertThat(created.id).isGreaterThan(0);
      assertThat(created.createdAt).isCloseTo(now, within(5, SECONDS));
      assertThat(created.balance).isEqualByComparingTo(balance);

      // When get by id
      Response<AccountJson> accountRs = api.account(created.id).execute();
      // Then
      assertOk(accountRs);
      assertThat(accountRs.body()).isEqualToComparingFieldByField(created);

      // When get all
      Response<List<AccountJson>> accountsRs = api.accounts().execute();
      // Then
      assertOk(accountsRs);
      assertThat(accountsRs.body())
          .hasSize(1)
          .allSatisfy(json -> assertThat(json).isEqualToComparingFieldByField(created));
    }

    @ParameterizedTest(name = "initial balance = {0}")
    @CsvSource({"-0.01", "-1", "-2.1", "-3.14", "12.345", "foo", "null"})
    @DisplayName("cannot create account with bad balance")
    void createMalformedAccount(String balance) throws Exception {
      // Given
      BadCreateAccountRequest rq = new BadCreateAccountRequest(
          "null".equals(balance) ? null : balance
      );

      // When create
      Response<AccountJson> createRs = api.createAccount(rq).execute();
      // Then
      assertBadRequest(createRs);

      // When get all
      Response<List<AccountJson>> accountsRs = api.accounts().execute();
      // Then
      assertOk(accountsRs);
      assertThat(accountsRs.body()).isEmpty();
    }

    @RepeatedTest(3)
    @DisplayName("created accounts can be retrieved")
    void createSeveralAccounts() throws Exception {
      // Given
      int n = ThreadLocalRandom.current().nextInt(2, 10);
      ImmutableList<CreateAccountRequest> rqs =
          ThreadLocalRandom.current()
              .ints(n, 100, 500)
              .mapToObj(balance -> new CreateAccountRequest(new BigDecimal(balance)))
              .collect(toImmutableList());
      Instant now = Instant.now();

      // When create
      ImmutableList<Response<AccountJson>> createRss =
          rqs.stream()
              .map(rq -> exec(api.createAccount(rq)))
              .collect(toImmutableList());
      // Then
      assertThat(createRss).allSatisfy(createRs -> {
        AccountJson created = createRs.body();
        assertOk(createRs);
        assertThat(created.id).isGreaterThan(0);
        assertThat(created.createdAt).isCloseTo(now, within(5, SECONDS));
        assertThat(created.balance.intValue()).isBetween(100, 499);
      });

      // When get by id
      ImmutableList<Response<AccountJson>> accountRss =
          createRss.stream()
              .map(createRs -> exec(api.account(createRs.body().id)))
              .collect(toImmutableList());
      // Then
      assertThat(accountRss).allSatisfy(MoneyTransferApiTest::assertOk);

      // When get all
      Response<List<AccountJson>> accountsRs = api.accounts().execute();
      // Then
      assertOk(accountsRs);
      assertThat(accountsRs.body())
          .hasSize(n)
          .extracting(rs -> rs.balance.intValue())
          .containsExactlyInAnyOrderElementsOf(
              rqs.stream()
                  .map(rq -> rq.balance.intValue())
                  .collect(toImmutableList())
          );
    }
  }

  @Nested
  class Transfers {
    @Test
    @DisplayName("empty response when no transfers made")
    void noTransfers() throws Exception {
      Response<List<TransferJson>> response = api.transfers().execute();

      assertOk(response);
      assertThat(response.body()).isEmpty();
    }

    @Test
    @DisplayName("new account has no transfers")
    void transfersOnNewAccount() throws Exception {
      long id = createAccount(new CreateAccountRequest(BigDecimal.ZERO));

      Response<List<TransferJson>> response = api.accountTransfers(id).execute();

      assertOk(response);
      assertThat(response.body()).isEmpty();
    }

    @Test
    @DisplayName("404 when get transfers from unknown account")
    void transfersOnUnknownAccount() throws Exception {
      Response<List<TransferJson>> response = api.accountTransfers(123456).execute();

      assertNotFound(response);
    }

    @Test
    @DisplayName("404 when transfer does not exist")
    void noTransfer() throws Exception {
      Response<TransferJson> response = api.transfer("42").execute();

      assertNotFound(response);
    }

    @Test
    @DisplayName("bad request when transfer id is malformed")
    void badGetTransfer() throws Exception {
      Response<TransferJson> response = api.transfer("bad").execute();

      assertBadRequest(response);
    }

    @Test
    @DisplayName("404 when transfer from unknown account")
    void unknownFromAccount() throws Exception {
      long id = createAccount(new CreateAccountRequest(BigDecimal.TEN));
      MakeTransferRequest unknownFrom = new MakeTransferRequest(1337L, id, BigDecimal.ONE);

      Response<TransferJson> rs = api.makeTransfer(unknownFrom).execute();

      assertNotFound(rs);
    }

    @Test
    @DisplayName("404 when transfer to unknown account")
    void unknownToAccount() throws Exception {
      long id = createAccount(new CreateAccountRequest(BigDecimal.TEN));
      MakeTransferRequest unknownTo = new MakeTransferRequest(id, 9000L, BigDecimal.ONE);

      Response<TransferJson> rs = api.makeTransfer(unknownTo).execute();

      assertNotFound(rs);
    }

    @Test
    @DisplayName("self transfer is bad request")
    void selfTransfer() throws Exception {
      long id = createAccount(new CreateAccountRequest(BigDecimal.TEN));
      MakeTransferRequest selfTransfer = new MakeTransferRequest(id, id, BigDecimal.ONE);

      Response<TransferJson> rs = api.makeTransfer(selfTransfer).execute();

      assertBadRequest(rs);
    }

    @ParameterizedTest(name = "transfer {2} from {0} to {1}")
    @CsvSource({
        // bad amount
        "?, ?, null",
        "?, ?, bad",
        "?, ?, 0",
        "?, ?, 1.234",
        "?, ?, -1",

        // bad from
        "null, ?, 1.00",
        "bad, ?, 1.00",

        // bad to
        "?, bad, 1.00",
        "?, null, 1.00",

        // not enough money
        "?, ?, 10.01"
    })
    @DisplayName("cannot make transfer with bad request")
    void makeBadTransfer(String from, String to, String amount) throws Exception {
      // Given
      String fromId = String.valueOf(
          createAccount(new CreateAccountRequest(BigDecimal.TEN))
      );
      String toId = String.valueOf(
          createAccount(new CreateAccountRequest(BigDecimal.TEN))
      );

      BadMakeTransferRequest rq = new BadMakeTransferRequest(
          "null".equals(from) ? null : from.replace("?", fromId),
          "null".equals(to) ? null : to.replace("?", toId),
          "null".equals(amount) ? null : amount
      );

      // When make transfer
      Response<TransferJson> makeRs = api.makeTransfer(rq).execute();
      // Then
      assertBadRequest(makeRs);

      // When get all
      Response<List<TransferJson>> accountsRs = api.transfers().execute();
      // Then
      assertOk(accountsRs);
      assertThat(accountsRs.body()).isEmpty();
    }

    @ParameterizedTest(name = "transfer {2} from {0} to {1} -> expect {3} and {4}")
    @CsvSource({
        "10, 0, 0.5, 9.5, 0.5",
        "9, 8, 1, 8, 9",
        "3.33, 2.22, 1.01, 2.32, 3.23",
        "10, 0, 10, 0, 10"
    })
    @DisplayName("make transfer and retrieve it after")
    void makeTransfer(
        BigDecimal initialFrom,
        BigDecimal initialTo,
        BigDecimal amount,
        BigDecimal expectedFrom,
        BigDecimal expectedTo
    ) throws Exception {
      // Given
      long fromId = createAccount(new CreateAccountRequest(initialFrom));
      long toId = createAccount(new CreateAccountRequest(initialTo));
      Instant now = Instant.now();

      MakeTransferRequest rq = new MakeTransferRequest(
          fromId,
          toId,
          amount
      );

      // When make transfer
      Response<TransferJson> makeRs = api.makeTransfer(rq).execute();
      TransferJson t = makeRs.body();
      // Then
      assertOk(makeRs);
      assertThat(t.id).isGreaterThan(0);
      assertThat(t.from).isEqualTo(fromId);
      assertThat(t.to).isEqualTo(toId);
      assertThat(t.at).isCloseTo(now, within(5, SECONDS));
      assertThat(t.amount).isEqualByComparingTo(amount);

      // When get by id
      Response<TransferJson> transferRs = api.transfer(t.id).execute();
      // Then
      assertOk(transferRs);
      assertThat(transferRs.body()).isEqualToComparingFieldByField(t);

      // When get all
      Response<List<TransferJson>> transfersRs = api.transfers().execute();
      // Then
      assertOk(transfersRs);
      assertThat(transfersRs.body())
          .hasSize(1)
          .allSatisfy(json -> {
            assertOk(transferRs);
            assertThat(json).isEqualToComparingFieldByField(t);
          });

      // When check from account
      Response<AccountJson> fromAccountRs = api.account(fromId).execute();
      Response<List<TransferJson>> fromAccountTransfersRs = api.accountTransfers(fromId).execute();
      // Then
      assertOk(fromAccountRs);
      assertThat(fromAccountRs.body().balance).isEqualByComparingTo(expectedFrom);

      assertOk(fromAccountTransfersRs);
      assertThat(fromAccountTransfersRs.body())
          .hasSize(1)
          .allSatisfy(
              transfer -> assertThat(transfer).isEqualToComparingFieldByField(t)
          );

      // When check to account
      Response<AccountJson> toAccountRs = api.account(toId).execute();
      Response<List<TransferJson>> toAccountTransfersRs = api.accountTransfers(toId).execute();
      // Then
      assertOk(toAccountRs);
      assertThat(toAccountRs.body().balance).isEqualByComparingTo(expectedTo);

      assertOk(toAccountTransfersRs);
      assertThat(toAccountTransfersRs.body())
          .hasSize(1)
          .allSatisfy(
              transfer -> assertThat(transfer).isEqualToComparingFieldByField(t)
          );
    }

    @Test
    @DisplayName("make chained transfers (a -> b -> c -> a)")
    void makeChainedTransfers(
    ) throws Exception {
      // Given
      CreateAccountRequest createAccountRq = new CreateAccountRequest(BigDecimal.TEN);
      long a = createAccount(createAccountRq);
      long b = createAccount(createAccountRq);
      long c = createAccount(createAccountRq);

      MakeTransferRequest a2b = new MakeTransferRequest(a, b, new BigDecimal(1)); // 9 11 10
      MakeTransferRequest b2c = new MakeTransferRequest(b, c, new BigDecimal(3)); // 9 8 13
      MakeTransferRequest c2a = new MakeTransferRequest(c, a, new BigDecimal(6)); // 15 8 7

      // When make transfers
      Response<TransferJson> a2bRs = api.makeTransfer(a2b).execute();
      Response<TransferJson> b2cRs = api.makeTransfer(b2c).execute();
      Response<TransferJson> c2aRs = api.makeTransfer(c2a).execute();
      // Then
      assertOk(a2bRs);
      assertOk(b2cRs);
      assertOk(c2aRs);

      // When get all
      Response<List<TransferJson>> transfersRs = api.transfers().execute();
      // Then
      assertOk(transfersRs);
      assertThat(transfersRs.body())
          .extracting(this::toTuple)
          .containsExactlyInAnyOrderElementsOf(
              Stream.of(a2b, b2c, c2a)
                  .map(this::toTuple)
                  .collect(toImmutableList())
          );

      // When check A account
      Response<AccountJson> aRs = api.account(a).execute();
      Response<List<TransferJson>> aTransfersRs = api.accountTransfers(a).execute();
      // Then
      assertOk(aRs);
      assertThat(aRs.body().balance).isEqualByComparingTo(new BigDecimal(15));

      assertOk(aTransfersRs);
      assertThat(aTransfersRs.body())
          .hasSize(2)
          .extracting(this::toTuple)
          .containsExactly(toTuple(c2a), toTuple(a2b));

      // When check B account
      Response<AccountJson> bRs = api.account(b).execute();
      Response<List<TransferJson>> bTransfersRs = api.accountTransfers(b).execute();
      // Then
      assertOk(bRs);
      assertThat(bRs.body().balance).isEqualByComparingTo(new BigDecimal(8));

      assertOk(bTransfersRs);
      assertThat(bTransfersRs.body())
          .hasSize(2)
          .extracting(this::toTuple)
          .containsExactly(toTuple(b2c), toTuple(a2b));

      // When check C account
      Response<AccountJson> cRs = api.account(c).execute();
      Response<List<TransferJson>> cTransfersRs = api.accountTransfers(c).execute();
      // Then
      assertOk(cRs);
      assertThat(cRs.body().balance).isEqualByComparingTo(new BigDecimal(7));

      assertOk(cTransfersRs);
      assertThat(cTransfersRs.body())
          .hasSize(2)
          .extracting(this::toTuple)
          .containsExactly(toTuple(c2a), toTuple(b2c));
    }

    private Tuple toTuple(MakeTransferRequest rq) {
      return tuple(rq.from, rq.to, rq.amount.intValue());
    }

    private Tuple toTuple(TransferJson rs) {
      return tuple(rs.from, rs.to, rs.amount.intValue());
    }

    private long createAccount(CreateAccountRequest createAccountRq) {
      return exec(api.createAccount(createAccountRq)).body().id;
    }

    /**
     * 0) Prepare abstract account id vector
     * 1) Prepare transfer templates based on vector indexes
     * 2) Perform transfers based on templates sequentially
     * 3) Perform transfers based on templates concurrently
     * 4) Compare resulting balance vectors
     */
    @RepeatedTest(10)
    @DisplayName("result of concurrent transfer execution should be same with sequential one")
    void concurrentTransfers() throws Exception {
      // Given
      final int n = Runtime.getRuntime().availableProcessors() * 4;
      CreateAccountRequest createAccountRq = new CreateAccountRequest(new BigDecimal(1_000_000));
      ImmutableList<BigDecimal> initialBalances =
          Stream.generate(() -> createAccountRq.balance)
              .limit(n)
              .collect(toImmutableList());

      ImmutableList<Integer> indexes =
          IntStream.range(0, n)
              .boxed()
              .collect(toImmutableList());


      int limit = indexes.size() * indexes.size() / 2;
      List<MakeTransferRequest> candidates =
          Sets.cartesianProduct(ImmutableSet.copyOf(indexes), ImmutableSet.copyOf(indexes))
              .stream()
              .filter(pair -> !Objects.equals(pair.get(0), pair.get(1)))
              .map(pair -> new MakeTransferRequest(
                  (long) pair.get(0),
                  (long) pair.get(1),
                  BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 5))
                      .setScale(2, RoundingMode.HALF_UP)
              ))
              .collect(toList());
      Collections.shuffle(candidates);
      ImmutableList<MakeTransferRequest> transferTemplates = ImmutableList.copyOf(
          candidates.subList(0, limit)
      );

      // When
      // sequential part
      ImmutableList<Long> seqAccounts =
          Stream.generate(() -> createAccount(createAccountRq))
              .limit(n)
              .collect(toImmutableList());

      transferTemplates.stream()
          .map(template -> new MakeTransferRequest(
              seqAccounts.get(template.from.intValue()),
              seqAccounts.get(template.to.intValue()),
              template.amount
          ))
          .forEach(
              rq -> assertOk(exec(api.makeTransfer(rq)))
          );

      ImmutableList<BigDecimal> seqBalances = seqAccounts.stream()
          .map(id -> exec(api.account(id)).body().balance)
          .collect(toImmutableList());

      // concurrent part
      ImmutableList<Long> concurrentAccounts =
          Stream.generate(() -> createAccount(createAccountRq))
              .limit(n)
              .collect(toImmutableList());

      CompletableFuture<?>[] fs =
          transferTemplates.stream()
              .map(template -> new MakeTransferRequest(
                  concurrentAccounts.get(template.from.intValue()),
                  concurrentAccounts.get(template.to.intValue()),
                  template.amount
              ))
              .map(rq -> CompletableFuture.runAsync(
                  () -> assertOk(exec(api.makeTransfer(rq))),
                  executor
              ))
              .toArray(CompletableFuture<?>[]::new);

      CompletableFuture.allOf(fs).get(1, MINUTES);

      ImmutableList<BigDecimal> concurrentBalances = concurrentAccounts.stream()
          .map(id -> exec(api.account(id)).body().balance)
          .collect(toImmutableList());

      // Then
      assertThat(concurrentBalances)
          .isEqualTo(seqBalances)
          .isNotEqualTo(initialBalances);
    }


    @RepeatedTest(10)
    @DisplayName("balance never goes below zero")
    void balanceNeverGoesBelowZero() throws Exception {
      // Given
      CreateAccountRequest createAccountRq = new CreateAccountRequest(new BigDecimal(500));
      long from = createAccount(createAccountRq);
      long to = createAccount(createAccountRq);

      // When
      Stream<MakeTransferRequest> requests = Stream.generate(() -> new MakeTransferRequest(
          from,
          to,
          BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 2))
              .setScale(2, RoundingMode.HALF_UP)
      ));
      CompletableFuture<List<Response<TransferJson>>> cf =
          requests
              .limit(createAccountRq.balance.intValue() + 1)
              .map(
                  rq -> CompletableFuture.supplyAsync(
                      () -> exec(api.makeTransfer(rq)),
                      executor
                  )
              ).collect(joinList());

      List<Response<TransferJson>> rss = cf.get(1, MINUTES);

      // Then
      assertThat(rss)
          .extracting(Response::code)
          .allMatch(code -> code < 500);

      BigDecimal fromBalance = api.account(from).execute().body().balance;
      BigDecimal toBalance = api.account(to).execute().body().balance;

      assertThat(fromBalance).isNotNegative();
      assertThat(toBalance).isNotNegative();
      assertThat(fromBalance.add(toBalance))
          .isEqualByComparingTo(createAccountRq.balance.multiply(new BigDecimal(2)));
    }

    @RepeatedTest(10)
    @DisplayName("should perform bidirectional transfers")
    void bidirectionalTransfers() throws Exception {
      // Given
      CreateAccountRequest createAccountRq = new CreateAccountRequest(new BigDecimal(10_000));
      long a = createAccount(createAccountRq);
      long b = createAccount(createAccountRq);

      // When
      Stream<MakeTransferRequest> requests =
          IntStream.range(0, 500)
              .mapToObj(i -> {
                long from, to;
                if (i % 2 == 0) {
                  from = a;
                  to = b;
                } else {
                  from = b;
                  to = a;
                }

                return new MakeTransferRequest(
                    from,
                    to,
                    BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 2))
                        .setScale(2, RoundingMode.HALF_UP)
                );
              });


      CompletableFuture<List<Response<TransferJson>>> cf =
          requests
              .map(
                  rq -> CompletableFuture.supplyAsync(
                      () -> exec(api.makeTransfer(rq)),
                      executor
                  )
              ).collect(joinList());

      List<Response<TransferJson>> rss = cf.get(1, MINUTES);

      // Then
      assertThat(rss)
          .extracting(Response::code)
          .allMatch(code -> code < 500);

      BigDecimal aBalance = api.account(a).execute().body().balance;
      BigDecimal bBalance = api.account(b).execute().body().balance;

      assertThat(aBalance.add(bBalance))
          .isEqualByComparingTo(createAccountRq.balance.multiply(new BigDecimal(2)));
    }
  }

  private static void assertOk(Response<?> response) {
    assertApplicationJson(response);
    assertThat(response.code()).isEqualTo(HttpStatus.OK_200);
  }

  private static void assertApplicationJson(Response<?> response) {
    assertThat(response.headers().get("Content-Type")).isEqualTo("application/json");
  }

  private static void assertNotFound(Response<?> response) throws IOException {
    assertApplicationJson(response);
    assertThat(response.code()).isEqualTo(HttpStatus.NOT_FOUND_404);
    assertThat(response.errorBody().string()).isNotEmpty();
  }

  private static void assertBadRequest(Response<?> response) throws IOException {
    assertApplicationJson(response);
    assertThat(response.code()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    assertThat(response.errorBody().string()).isNotEmpty();
  }

  private static <T> Response<T> exec(Call<T> call) {
    try {
      return call.execute();
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
  }

  public interface Api {

    @GET("accounts")
    Call<List<AccountJson>> accounts();

    @POST("accounts")
    Call<AccountJson> createAccount(@Body CreateAccountRequest rq);

    @POST("accounts")
    Call<AccountJson> createAccount(@Body BadCreateAccountRequest rq);

    @GET("accounts/{id}")
    Call<AccountJson> account(@Path("id") String id);

    @GET("accounts/{id}")
    Call<AccountJson> account(@Path("id") long id);

    @GET("accounts/{id}/transfers")
    Call<List<TransferJson>> accountTransfers(@Path("id") long id);

    @GET("transfers")
    Call<List<TransferJson>> transfers();

    @POST("transfers")
    Call<TransferJson> makeTransfer(@Body MakeTransferRequest rq);

    @POST("transfers")
    Call<TransferJson> makeTransfer(@Body BadMakeTransferRequest rq);

    @GET("transfers/{id}")
    Call<TransferJson> transfer(@Path("id") String id);

    @GET("transfers/{id}")
    Call<TransferJson> transfer(@Path("id") long id);

  }

  static class BadCreateAccountRequest {

    public final String balance;

    BadCreateAccountRequest(String balance) {
      this.balance = balance;
    }
  }

  static class BadMakeTransferRequest {
    public final String from;
    public final String to;
    public final String amount;

    BadMakeTransferRequest(String from, String to, String amount) {
      this.from = from;
      this.to = to;
      this.amount = amount;
    }
  }

  static class AccountJson {

    public long id;
    public Instant createdAt;
    public BigDecimal balance;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("createdAt", createdAt)
          .add("balance", balance)
          .toString();
    }
  }

  static class TransferJson {

    public long id;
    public long from;
    public long to;
    public Instant at;
    public BigDecimal amount;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("from", from)
          .add("to", to)
          .add("at", at)
          .add("amount", amount)
          .toString();
    }
  }

  private static Api createApi() {
    return new Retrofit.Builder()
        .baseUrl("http://localhost:8080/api/v1/")
        .addConverterFactory(JacksonConverterFactory.create(
            new ObjectMapper().findAndRegisterModules()
        ))
        .build()
        .create(Api.class);
  }

}

