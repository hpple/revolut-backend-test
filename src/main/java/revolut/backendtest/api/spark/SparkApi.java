package revolut.backendtest.api.spark;

import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;
import static spark.Spark.after;
import static spark.Spark.awaitInitialization;
import static spark.Spark.awaitStop;
import static spark.Spark.before;
import static spark.Spark.defaultResponseTransformer;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.initExceptionHandler;
import static spark.Spark.path;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.stop;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.backendtest.api.BadRequest;
import revolut.backendtest.api.NotFound;
import revolut.backendtest.api.RestApi;
import spark.ResponseTransformer;

public class SparkApi implements RestApi {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final ResponseTransformer transformer;

  private final AccountController accounts;
  private final TransferController transfers;
  private final ExceptionHandlers exceptionHandlers;

  @Inject
  public SparkApi(
      ResponseTransformer transformer,
      AccountController accounts,
      TransferController transfers,
      ExceptionHandlers exceptionHandlers
  ) {
    this.transformer = transformer;
    this.accounts = accounts;
    this.transfers = transfers;
    this.exceptionHandlers = exceptionHandlers;
  }

  @Override
  public void startUp() {
    port(8080);
    defaultResponseTransformer(transformer);
    initExceptionHandler(ex -> log.error("Failed to init", ex));

    path(
        "/api/v1",
        () -> {
          before(
              "/*",
              (rq, rs) -> log.info(
                  "Incoming request: {} {} {}",
                  rq.requestMethod(),
                  rq.pathInfo(),
                  rq.body()
              )
          );

          get("/accounts", accounts::getAccounts);
          get("/accounts/:id", accounts::getAccount);
          get("/accounts/:id/transfers", accounts::getAccountTransfers);
          post("/accounts", accounts::createAccount);

          get("/transfers", transfers::getTransfers);
          get("/transfers/:id", transfers::getTransfer);
          post("/transfers", transfers::makeTransfer);

          after(
              "/*",
              (rq, rs) -> {
                rs.type(APPLICATION_JSON.asString());
                log.info(
                    "Outgoing response: {} {} -> {} {}",
                    rq.requestMethod(),
                    rq.pathInfo(),
                    rs.status(),
                    rs.body()
                );
              }
          );
        }
    );

    exception(BadRequest.class, exceptionHandlers::badRequest);
    exception(NotFound.class, exceptionHandlers::notFound);
    exception(Exception.class, exceptionHandlers::serverError);

    awaitInitialization();
  }

  @Override
  public void shutDown() {
    stop();
    awaitStop();
  }
}
