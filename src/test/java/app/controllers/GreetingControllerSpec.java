package app.controllers;

import org.javalite.activeweb.ControllerSpec;
import app.services.GreeterMockModule;
import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

/**
 * @author
 */
public class GreetingControllerSpec extends ControllerSpec {

    /*
    @Before
    public void before(){
        setInjector(Guice.createInjector(new GreeterMockModule()));
    }

    @Test
    public void shouldUseMockService(){
        //call controller
        request().get("index");
        //verify value assigned from controller to view.
        the(assigns().get("greeting")).shouldBeEqual("Hello from class app.services.GreeterMock");
    }
    */
}
