package revolut.backendtest.persistence.jdbc;

import com.google.common.util.concurrent.AbstractIdleService;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.util.Optional;
import javax.inject.Inject;

public class DatabaseService extends AbstractIdleService implements DataSourceProvider {

  private final JdbcConfig config;

  private Optional<HikariDataSource> dataSource = Optional.empty();

  @Inject
  public DatabaseService(JdbcConfig config) {
    this.config = config;
  }

  @Override
  protected void startUp() {
    HikariConfig hikariConfig = new HikariConfig();
    hikariConfig.setJdbcUrl(config.url);
    hikariConfig.setUsername(config.user);
    hikariConfig.setPassword(config.password);
    hikariConfig.setAutoCommit(false);

    dataSource = Optional.of(new HikariDataSource(hikariConfig));
  }

  @Override
  protected void shutDown() {
    dataSource.ifPresent(HikariDataSource::close);
  }

  @Override
  public HikariDataSource dataSource() {
    return dataSource.orElseThrow(() -> new IllegalStateException("DatabaseService not started"));
  }

}