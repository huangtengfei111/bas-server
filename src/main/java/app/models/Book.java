package app.models;

import org.javalite.activejdbc.Model;

/**
 * @author 
 */
public class Book extends Model {
    static {
        validatePresenceOf("title", "isbn");
        validatePresenceOf("author").message("Author must be provided");
    }
}
