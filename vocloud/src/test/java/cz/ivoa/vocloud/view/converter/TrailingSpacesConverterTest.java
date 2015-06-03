package cz.ivoa.vocloud.view.converter;

import java.util.Random;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Testing Converter class that is used by JSF framework to convert strings in
 * input text fields in the presentation tier into real String instances of JSF
 * backing beans. This converter is used to trim trailing spaces on the
 * beginning and in the end of the passed string. Note that parameters context
 * and component are not used by the converter and so it is necessary to pass an
 * implementation of them to converter methods.
 *
 * @author radio.koza
 */
public class TrailingSpacesConverterTest {

    private TrailingSpacesConverter converter;
    private FacesContext context;
    private UIComponent component;

    @Before
    public void setUp() {
        converter = new TrailingSpacesConverter();//note that instanciation of this object is usually done by dependency injection
        context = null;
        component = null;
    }

    private static final String whitechars = " \n\t\r";

    /**
     * Generate random string without white characters on the beginning and in
     * the end
     *
     * @return Trimmed result String
     */
    private String generateTrimmedString() {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        int textLength = random.nextInt(20);
        if (textLength == 0) {
            return "";
        }
        if (textLength == 1) {
            return "" + (char) (random.nextInt(126 - 33 + 1) + 33);
        }
        text.append((char) (random.nextInt(126 - 33 + 1) + 33));
        for (int i = 1; i < textLength - 1; i++) {
            if (random.nextBoolean()) {
                //append whitechar
                text.append(whitechars.charAt(random.nextInt(whitechars.length())));
            } else {
                //append text char
                text.append((char) (random.nextInt(126 - 33 + 1) + 33));
            }
        }
        text.append((char) (random.nextInt(126 - 33 + 1) + 33));
        return text.toString();
    }

    /**
     * Enclose the trimmed string into randomly generated white characters
     *
     * @param trimmed Trimmed string to be enclosed
     * @return New trimmable string
     */
    private String generateUntrimmedStringFromTrimmed(String trimmed) {
        Random random = new Random();
        int beforeLen = random.nextInt(5);
        int afterLen = random.nextInt(5);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < beforeLen; i++) {
            result.append(whitechars.charAt(random.nextInt(whitechars.length())));
        }
        result.append(trimmed);
        for (int i = 0; i < afterLen; i++) {
            result.append(whitechars.charAt(random.nextInt(whitechars.length())));
        }
        return result.toString();
    }

    /**
     * Test of getAsObject method, of class TrailingSpacesConverter. Method
     * converts string from input text field in presentation tier into Java
     * object with trimmed trailing whitespaces.
     */
    @Test
    public void testGetAsObject() {
        System.out.println("getAsObject");
        final int testCount = 5000;
        String untrimmed;
        String trimmed;
        String result;
        for (int i = 0; i < testCount; i++) {
            trimmed = generateTrimmedString();
            untrimmed = generateUntrimmedStringFromTrimmed(trimmed);
            result = (String) converter.getAsObject(context, component, untrimmed);
            assertEquals(trimmed, result);
        }
    }

    /**
     * Test of getAsString method, of class TrailingSpacesConverter. This method
     * must leave String as it is including trailing whitespaces.
     */
    @Test
    public void testGetAsString() {
        System.out.println("getAsString");
        final int testCount = 5000;
        String untrimmed;
        String result;
        for (int i = 0; i < testCount; i++) {
            untrimmed = generateUntrimmedStringFromTrimmed(generateTrimmedString());
            result = converter.getAsString(context, component, untrimmed);
            assertEquals(untrimmed, result);
        }
    }

}
