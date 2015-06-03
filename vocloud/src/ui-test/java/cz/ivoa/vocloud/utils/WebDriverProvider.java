package cz.ivoa.vocloud.utils;

import java.util.logging.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

/**
 *
 * @author radio.koza
 */
public class WebDriverProvider {

    private static final Logger LOG = Logger.getLogger(WebDriverProvider.class.getName());

    private static WebDriver driver;

    private WebDriverProvider() {

    }

    static {
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
    }

    public static WebDriver getWebDriver() {
        if (driver == null) {
            LOG.info("Opening Chrome driver");
            driver = new ChromeDriver();
            driver.manage().window().maximize();
        }
        return driver;
    }

    public static void closeCurrentDriver() {
        if (driver != null) {
            LOG.info("Closing driver");
            driver.close();
            driver = null;
        }
    }
}
