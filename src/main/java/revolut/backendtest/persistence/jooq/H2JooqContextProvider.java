package revolut.backendtest.persistence.jooq;

import com.google.inject.Inject;
import java.sql.Connection;
import java.sql.SQLException;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.jooq.impl.DefaultConfiguration;
import revolut.backendtest.persistence.jdbc.DataSourceProvider;

public class H2JooqContextProvider implements JooqContextProvider {

  private final DSLContext ctx;

  @Inject
  public H2JooqContextProvider(DataSourceProvider dataSourceProvider) {
    ctx = DSL.using(
        new DefaultConfiguration()
            .set(
                new ConnectionProvider() {
                  @Override
                  public Connection acquire() throws DataAccessException {
                    try {
                      return dataSourceProvider.dataSource().getConnection();
                    } catch (SQLException ex) {
                      throw new DataAccessException("unable to get connection from pool", ex);
                    }
                  }

                  @Override
                  public void release(Connection connection) throws DataAccessException {
                    try {
                      connection.close();
                    } catch (SQLException ex) {
                      throw new DataAccessException("exception while close connection", ex);
                    }
                  }
                }
            )
            .set(SQLDialect.H2)
    );
  }

  @Override
  public DSLContext context() {
    return ctx;
  }

}

