package cz.ivoa.pageobjects;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 *
 * @author radio.koza
 */
public class LoginPage extends AbstractPage{
    private static final Logger LOG = Logger.getLogger(LoginPage.class.getName());
    
    @FindBy(xpath = "//label[text()='User name:']/../..//input")
    private WebElement usernameField;
    
    @FindBy(xpath = "//label[text()='Password:']/../..//input")
    private WebElement passwordField;
    
    @FindBy(xpath = "//span[text()='Login']/../../button")
    private WebElement loginButton;
    
    public LoginPage(WebDriver driver) {
        super(driver);
        //wait for the page to load
        waitForObject(By.xpath("//span[text()='Login']/../../button"));
        PageFactory.initElements(driver, this);
    }
    
    public LoginPage setUsername(String username){
        LOG.log(Level.INFO, "Setting username: {0}", username);
        usernameField.sendKeys(username);
        return this;
    }
    
    public LoginPage setPassword(String password){
        LOG.log(Level.INFO, "Setting password: {0}", password);
        passwordField.sendKeys(password);
        return this;
    }
    
    public void login(){
        LOG.info("Clicking Login button");
        loginButton.click();
    }
    
}
