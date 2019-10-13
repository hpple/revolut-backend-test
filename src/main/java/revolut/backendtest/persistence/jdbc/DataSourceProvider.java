package revolut.backendtest.persistence.jdbc;

import javax.sql.DataSource;

public interface DataSourceProvider {

  DataSource dataSource();
}
