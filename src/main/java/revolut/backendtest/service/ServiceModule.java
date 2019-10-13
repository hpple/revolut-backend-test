package revolut.backendtest.service;

import com.google.inject.PrivateModule;

public class ServiceModule extends PrivateModule {

  @Override
  protected void configure() {
    bind(AccountService.class).asEagerSingleton();
    expose(AccountService.class);
    bind(TransferService.class).asEagerSingleton();
    expose(TransferService.class);
  }
}
