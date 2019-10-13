package revolut.backendtest;

import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MoneyTransferApplication {

  private final Logger log = LoggerFactory.getLogger(getClass());

  private final MoneyTransferServer server = new MoneyTransferServer();

  public static void main(String[] args) throws Exception {
    new MoneyTransferApplication().start();
  }

  private void start() throws TimeoutException {
    Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
    log.info("Starting...");
    server.start();
    log.info("Started.");
  }

  private void stop() {
    log.info("Stopping...");
    server.stop();
    log.info("Stopped.");
  }
}

