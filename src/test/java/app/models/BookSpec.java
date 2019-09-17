package app.models;

import org.javalite.activeweb.DBSpec;
import org.junit.Test;

import java.util.List;

/**
 * @author 
 */
public class BookSpec extends DBSpec {

    @Test
    public void shouldValidateRequiredAttributes(){
        Book book = new Book();
        a(book).shouldNotBe("valid");

        book.set("title", "fake title", "author", "fake author", "isbn", "12345");
        a(book).shouldBe("valid");
    }

}

