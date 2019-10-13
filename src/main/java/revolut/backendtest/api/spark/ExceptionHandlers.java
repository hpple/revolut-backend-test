package revolut.backendtest.api.spark;

import static org.eclipse.jetty.http.MimeTypes.Type.APPLICATION_JSON;

import com.google.inject.Inject;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.backendtest.api.dto.ErrorResponse;
import spark.Request;
import spark.Response;

public class ExceptionHandlers {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final JsonResponseTransformer transformer;

  @Inject
  public ExceptionHandlers(
      JsonResponseTransformer transformer
  ) {
    this.transformer = transformer;
  }

  void serverError(Exception ex, Request rq, Response rs) {
    sendErrorResponse(ex, rq, rs, HttpStatus.INTERNAL_SERVER_ERROR_500);
  }

  private void sendErrorResponse(Exception ex, Request rq, Response rs, int status) {
    rs.status(status);
    rs.type(APPLICATION_JSON.asString());
    rs.body(toErrorResponseJson(ex));
    log.error(
        "Outgoing error response: {} {} -> {} {} {}",
        rq.requestMethod(),
        rq.pathInfo(),
        rs.status(),
        rs.body(),
        ex
    );
  }

  void notFound(Exception ex, Request rq, Response rs) {
    sendErrorResponse(ex, rq, rs, HttpStatus.NOT_FOUND_404);
  }

  void badRequest(Exception ex, Request rq, Response rs) {
    sendErrorResponse(ex, rq, rs, HttpStatus.BAD_REQUEST_400);
  }

  private String toErrorResponseJson(Exception ex) {
    return transformer.render(new ErrorResponse(ex.getMessage()));
  }

}
