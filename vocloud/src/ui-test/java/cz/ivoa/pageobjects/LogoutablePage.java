package cz.ivoa.pageobjects;

import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 *
 * @author radio.koza
 */
public class LogoutablePage extends AbstractPage{
    private static final Logger LOG = Logger.getLogger(LogoutablePage.class.getName());

    protected final String username;
    
    public LogoutablePage(WebDriver driver, String username) {
        super(driver);
        this.username = username;
        waitForObject(By.xpath("//span[text() = 'Logout (" + username + ")']/../../a"));
    }
    
    public LoginPage logout() {
        LOG.info("Clicking Logout menu item");
        WebElement logoutMenuItem = driver.findElement(By.xpath("//span[text() = 'Logout (" + username + ")']/../../a"));
        logoutMenuItem.click();
        return new LoginPage(driver);
    }
    
}
