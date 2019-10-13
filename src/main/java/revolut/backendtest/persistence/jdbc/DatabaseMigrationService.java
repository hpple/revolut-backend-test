package revolut.backendtest.persistence.jdbc;

import com.google.common.util.concurrent.AbstractIdleService;
import com.google.inject.Inject;
import org.flywaydb.core.Flyway;

public class DatabaseMigrationService extends AbstractIdleService {

  private final DataSourceProvider dataSourceProvider;

  @Inject
  public DatabaseMigrationService(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  @Override
  protected void startUp() {
    Flyway.configure()
        .dataSource(dataSourceProvider.dataSource())
        .load()
        .migrate();
  }

  @Override
  protected void shutDown() {
  }
}

