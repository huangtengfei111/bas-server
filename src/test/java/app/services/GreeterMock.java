package app.services;

/**
 * @author 
 */
public class GreeterMock /*implements Greeter*/{
    public String greet() {
        return "Hello from " + this.getClass().toString();  
    }
}
