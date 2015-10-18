package cz.ivoa.vocloud.view.validator;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 * UrlValidator class is used in presentation tier by JSF framework to validate
 * correctness of URL addresses used in text inputs. Note that FacesContext and
 * UIComponent of the validation is not required by Validator class and may be
 * called with null parameter. There is only one method <code>validate</code>
 * that throws ValidatorException on validation failure. Supported protocols are
 * ftp, http and https
 *
 * @author radio.koza
 */
public class UrlValidatorTest {

    private static final Logger LOG = Logger.getLogger(UrlValidatorTest.class.getName());

    private UrlValidator validator;
    private FacesContext context;//passed as context of the JSF validation -- may be null in this case
    private UIComponent component;//passed as source component for JSF validation -- may be null in this case

    @Before
    public void setUp() {
        validator = new UrlValidator();//class is usually instanciated by JSF framework by using dependency injection
        context = null;
        component = null;
    }

//    /**
//     * Testing that empty URL string is invalid.
//     */
//    @Test(expected = ValidatorException.class)
//    public void testValidateEmptyString() {
//        LOG.info("validate empty URL string");
//        String value = "";
//        validator.validate(context, component, value);
//        fail("Validation passed but it should not");
//    }

    /**
     * Testing that URL string with one space is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateOneSpace() {
        LOG.info("validate whitespaces URL string");
        String value = " ";
        validator.validate(context, component, value);
        fail("Validation passed but is should not");
    }

    /**
     * Testing that URL string with more spaces is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateMoreSpaces() {
        LOG.info("validate more whitespaces URL string");
        String value = "      ";
        validator.validate(context, component, value);
        fail("Validation passed but is should not");
    }

    /**
     * Testing that URL string containing CRLF chars is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateCRLF() {
        LOG.info("validate URL string with CRLF");
        String value = "\r\n";
        validator.validate(context, component, value);
        fail("Validation passed but it should not");
    }

    /**
     * Testing that URL string containing only LF char is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testValidateLF() {
        LOG.info("validate URL string with LF");
        String value = "\n";
        validator.validate(context, component, value);
        fail("Validation passed but it should not");
    }

    /**
     * Testing that URLs with valid paths with protocol http are valid.
     */
    @Test
    public void testHttpProtocolValidity() {
        LOG.info("validate correct URL with HTTP protocol example");
        String[] values = {
            "http://www.seznam.cz",
            "http://seznam.cz",
            "http://seznam.cz/",
            "http://google.com",
            "http://127.0.0.1",
            "http://localhost",
            "http://localhost:80",
            "http://localhost:8080#baf",
            "http://localhost:9999/baf/.baf2?ahoj=cc&baf=foo",
            "http://asu.cas.cz:8800/~test/baf/tmp.html",};
        for (String value : values) {
            LOG.log(Level.INFO, "  testing URL: {0}", value);
            validator.validate(context, component, value);
        }
    }

    /**
     * Testing that URLs with valid paths with protocol https are valid.
     */
    @Test
    public void testHttpsProtocolValidity() {
        LOG.info("validate correct URL with HTTPS protocol example");
        String[] values = {
            "https://www.seznam.cz",
            "https://seznam.cz",
            "https://seznam.cz/",
            "https://google.com",
            "https://127.0.0.1",
            "https://localhost",
            "https://localhost:80",
            "https://localhost:8080#baf",
            "https://localhost:9999/baf/.baf2?ahoj=cc&baf=foo",
            "https://asu.cas.cz:8800/~test/baf/tmp.html",};
        for (String value : values) {
            LOG.log(Level.INFO, "  testing URL: {0}", value);
            validator.validate(context, component, value);
        }
    }

    /**
     * Testing that URLs with valid paths with protocol ftp are valid.
     */
    @Test
    public void testFtpProtocolValidity() {
        LOG.info("validate correct URL with FTP protocol example");
        String[] values = {
            "ftp://www.seznam.cz",
            "ftp://seznam.cz",
            "ftp://seznam.cz/",
            "ftp://google.com",
            "ftp://127.0.0.1",
            "ftp://localhost",
            "ftp://localhost:80",
            "ftp://localhost:8080",
            "ftp://localhost:9999/baf/.baf2?ahoj=cc&baf=foo",
            "ftp://asu.cas.cz:8800/~test/baf/tmp.html",};
        for (String value : values) {
            LOG.log(Level.INFO, "  testing URL: {0}", value);
            validator.validate(context, component, value);
        }
    }

    /**
     * Testing that URLs with valid paths with undefined protocol (considered as
     * simple http) are valid.
     */
    @Test
    public void testUndefinedProtocolValidity() {
        LOG.info("validate correct URL with undefined protocol example");
        //undefined protocol is considered as http
        String[] values = {
            "www.seznam.cz",
            "seznam.cz",
            "seznam.cz/",
            "google.com",
            "127.0.0.1",
            "localhost",
            "localhost:80",
            "localhost:8080#baf",
            "localhost:9999/baf/baf2?ahoj=cc&baf=foo",
            "asu.cas.cz:8800/~test/baf/tmp",};
        for (String value : values) {
            LOG.log(Level.INFO, "  testing URL: {0}", value);
            validator.validate(context, component, value);
        }
    }

    /**
     * Testing that URL containing space on the beginning is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testSpaceOnBeginning() {
        LOG.info("validate URL with space on the beginning");//should fail
        String value = " http://www.seznam.cz";
        validator.validate(context, component, value);
        fail("Validation passed but should not");
    }

    /**
     * Testing that URL containing space in the end is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testSpaceInTheEnd() {
        LOG.info("validate URL with space in the end");//should fail
        String value = "http://www.seznam.cz ";
        validator.validate(context, component, value);
        fail("Validation passed but should not");
    }

    /**
     * Testing that URL containing port with invalid value is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testInvalidPortNumber() {
        LOG.info("validate URL with invalid port number");
        String value = "http://asu.cas.cz:66000";
        validator.validate(context, component, value);
        fail("Validation passed but should not");
    }

    /**
     * Testing that URLs containing invalid characters are really invalid.
     */
    @Test
    public void testInvalidCharacters() {
        LOG.info("validate URL with invalid characters");
        char[] invalidChars = "{}|\\^[]`".toCharArray();
        String start = "http://foo";
        String end = "baf/lol";
        for (char c : invalidChars) {
            String value = start + c + end;
            LOG.log(Level.INFO, "  testing: {0}", value);
            try {
                validator.validate(context, component, value);
            } catch (ValidatorException ex) {
                continue;
            }
            fail("Validation passed but should not");
        }
    }

    /**
     * Testing that URL with unsupported protocol is invalid.
     */
    @Test(expected = ValidatorException.class)
    public void testInvalidProtocol() {
        LOG.info("validate URL with invalid protocol");
        String value = "ssh://asu.cas.cz";
        validator.validate(context, component, value);
        fail("Validation passed but should not");
    }
}
