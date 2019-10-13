package revolut.backendtest;

import static com.google.common.collect.Lists.reverse;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import revolut.backendtest.api.RestApiModule;
import revolut.backendtest.api.RestApiService;
import revolut.backendtest.persistence.PersistenceModule;
import revolut.backendtest.persistence.jdbc.DatabaseMigrationService;
import revolut.backendtest.persistence.jdbc.DatabaseService;
import revolut.backendtest.service.ServiceModule;

public class MoneyTransferServer {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final List<Service> startedServices = new ArrayList<>();

  static {
    calmDownRootLogger();
  }

  private static void calmDownRootLogger() {
    ch.qos.logback.classic.Logger root =
        (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    root.setLevel(Level.INFO);
  }

  public void start() throws TimeoutException {
    try {
      Injector injector = createInjector();

      for (Key<? extends Service> serviceKey : getServicesInStartUpOrder()) {
        Service service = injector.getInstance(serviceKey);
        start(service);
      }
    } catch (Throwable t) {
      log.error("Failed to start all services", t);
      stop();
      throw t;
    }
  }

  private void start(Service service) throws TimeoutException {
    log.info("Starting service: {}", service);

    startedServices.add(service);
    service.startAsync().awaitRunning(1, MINUTES);

    log.info("Started service: {}", service);
  }

  private Injector createInjector() {
    return Guice.createInjector(
        new PersistenceModule(),
        new ServiceModule(),
        new RestApiModule()
    );
  }

  private ImmutableList<Key<? extends Service>> getServicesInStartUpOrder() {
    return ImmutableList.<Key<? extends Service>>builder()
        .add(Key.get(DatabaseService.class))
        .add(Key.get(DatabaseMigrationService.class))
        .add(Key.get(RestApiService.class))
        .build();
  }

  public void stop() {
    log.info("Stopping server...");
    for (Service service : reverse(startedServices)) {
      try {
        stop(service);
      } catch (Throwable t) {
        log.error("Failed to stop service: " + service, t);
      }
    }
    log.info("Stopped server.");

  }

  private void stop(Service service) throws TimeoutException {
    log.info("Stopping service: {}", service);
    service.stopAsync().awaitTerminated(5, SECONDS);
    log.info("Stopped service: {}", service);
  }
}
