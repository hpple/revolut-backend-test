package revolut.backendtest.persistence.jooq;

import org.jooq.DSLContext;

public interface JooqContextProvider {

  DSLContext context();
}
