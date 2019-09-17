package app.services;

import com.google.inject.AbstractModule;

public class CitizenModule extends AbstractModule{

  @Override
  protected void configure() {
    bind(CitizenService.class).to(CitizenServiceImpl.class).asEagerSingleton();
  }

}
