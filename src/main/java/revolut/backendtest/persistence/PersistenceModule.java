package revolut.backendtest.persistence;

import com.google.inject.PrivateModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import revolut.backendtest.persistence.jdbc.DataSourceProvider;
import revolut.backendtest.persistence.jdbc.DatabaseMigrationService;
import revolut.backendtest.persistence.jdbc.DatabaseService;
import revolut.backendtest.persistence.jdbc.JdbcConfig;
import revolut.backendtest.persistence.jooq.H2JooqContextProvider;
import revolut.backendtest.persistence.jooq.JooqContextProvider;

public class PersistenceModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(DataSourceProvider.class).to(DatabaseService.class).asEagerSingleton();
    bind(DatabaseService.class).asEagerSingleton();
    expose(DatabaseService.class);

    bind(DatabaseMigrationService.class).asEagerSingleton();
    expose(DatabaseMigrationService.class);

    bind(JooqContextProvider.class).to(H2JooqContextProvider.class).asEagerSingleton();
    expose(JooqContextProvider.class);
  }

  @Provides
  @Singleton
  private JdbcConfig jdbcConfig() {
    //TODO: configure in file?
    return new JdbcConfig(
      "jdbc:h2:mem:revolut;DB_CLOSE_DELAY=-1",
      "sa",
      ""
    );
  }
}


