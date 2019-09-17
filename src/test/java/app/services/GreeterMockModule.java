package app.services;

import com.google.inject.AbstractModule;

/**
 * @author 
 */
public class GreeterMockModule extends AbstractModule {
    @Override
    protected void configure() {
        //bind(Greeter.class).to(GreeterMock.class).asEagerSingleton();
    }
}
