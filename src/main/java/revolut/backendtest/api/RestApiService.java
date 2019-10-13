package revolut.backendtest.api;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;

public class RestApiService extends AbstractIdleService {

  private final RestApi api;

  @Inject
  public RestApiService(RestApi api) {
    this.api = api;
  }

  @Override
  protected void startUp() {
    api.startUp();
  }

  @Override
  protected void shutDown() {
    api.shutDown();
  }
}

