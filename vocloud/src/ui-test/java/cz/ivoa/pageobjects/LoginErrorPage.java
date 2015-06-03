package cz.ivoa.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 *
 * @author radio.koza
 */
public class LoginErrorPage extends AbstractPage{
    
    
    @FindBy(xpath = "//span[text()='Login']/../../a")
    private WebElement loginMenuItem;

    public LoginErrorPage(WebDriver driver) {
        super(driver);
        //wait for load
        waitForObject(By.xpath("//span[text()='Login']/../../a"));
        if (!driver.getCurrentUrl().endsWith("/loginerror.xhtml")){
            throw new IllegalStateException("Not a loginerror page");
        }
        PageFactory.initElements(driver, this);
    }
    
    public LoginPage clickLogin(){
        loginMenuItem.click();
        return new LoginPage(driver);
    }
    
    
}
