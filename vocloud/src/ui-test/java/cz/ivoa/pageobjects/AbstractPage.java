package cz.ivoa.pageobjects;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/**
 *
 * @author radio.koza
 */
public class AbstractPage {
    private static final Logger LOG = Logger.getLogger(AbstractPage.class.getName());

    //timeout for pages
    protected static final int TIMEOUT = 10; //10 seconds

    protected final WebDriver driver;

    protected AbstractPage(WebDriver driver) {
        this.driver = driver;
    }

    protected void waitForObject(By identification) {
        LOG.log(Level.INFO, "Waiting for {0}", identification.toString());
        final WebDriverWait wait = new WebDriverWait(driver, TIMEOUT);
        wait.until(ExpectedConditions.visibilityOfElementLocated(identification));
    }

}
