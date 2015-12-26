import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void simpleCheck() {
    	int a = 2;
        assertEquals(a, 2);
    }

    @Test
    public void renderTemplate() {
        //Content html = views.html.index.render("Your new application is ready.");
        //assertThat(contentType(html)).isEqualTo("text/html");
        //assertThat(contentAsString(html)).contains("Your new application is ready.");
    }


}
