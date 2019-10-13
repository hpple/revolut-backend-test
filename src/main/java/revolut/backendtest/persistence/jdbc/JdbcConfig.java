package revolut.backendtest.persistence.jdbc;

public final class JdbcConfig {

  public final String url;
  public final String user;
  public final String password;

  public JdbcConfig(String url, String user, String password) {
    this.url = url;
    this.user = user;
    this.password = password;
  }

}
