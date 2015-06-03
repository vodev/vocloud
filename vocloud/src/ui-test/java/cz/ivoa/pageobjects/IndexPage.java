package cz.ivoa.pageobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 *
 * @author radio.koza
 */
public class IndexPage extends AbstractPage{
    
    @FindBy(xpath = "//span[text()='Login']/../../a")
    private WebElement loginMenuItem;
    
    public IndexPage(WebDriver driver){
        super(driver);
        driver.get("http://127.0.0.1/vocloud/");
        PageFactory.initElements(driver, this);
    }
    
    public LoginPage clickLoginMenuItem(){
        loginMenuItem.click();
        return new LoginPage(driver);
    }
    
}
