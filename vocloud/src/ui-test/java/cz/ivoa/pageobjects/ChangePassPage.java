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
public class ChangePassPage extends LogoutablePage {
    private static final Logger LOG = Logger.getLogger(ChangePassPage.class.getName());

    @FindBy(id = "settingsform:oldpass")
    private WebElement oldPass;
    
    @FindBy(id = "settingsform:newpass")
    private WebElement newPass1;
    
    @FindBy(id = "settingsform:newpassagain")
    private WebElement newPass2;
            
    @FindBy(xpath = "//span[text()='Submit']/../../button")
    private WebElement submitButton;
    
    public ChangePassPage(WebDriver driver, String username) {
        super(driver, username);
        PageFactory.initElements(driver, this);
    }
    
    public ChangePassPage setOldPass(String pass){
        LOG.log(Level.INFO, "Setting old password to: {0}", pass);
        oldPass.sendKeys(pass);
        return this;
    }
    
    public ChangePassPage setNewPass1(String pass){
        LOG.log(Level.INFO, "Setting new password 1 to: {0}", pass);
        newPass1.sendKeys(pass);
        return this;
    }
    
    public ChangePassPage setNewPass2(String pass){
        LOG.log(Level.INFO, "Setting new password 2 to: {0}", pass);
        newPass2.sendKeys(pass);
        return this;
    }
    
    public void clickSubmitButton(){
        LOG.info("Clicking submit button");
        submitButton.click();
    }
    
    
    public String getOldPassErrorDetail(){
        waitForObject(By.xpath("//td[text()='Old password:']/..//span[@class='ui-message-error-detail']"));
        WebElement element = driver.findElement(By.xpath("//td[text()='Old password:']/..//span[@class='ui-message-error-detail']"));
        return element.getText();
    }
    
    public String getPass1ErrorDetail(){
        WebElement element = driver.findElement(By.xpath("//td[text()='New password:']/..//span[@class='ui-message-error-detail']"));
        return element.getText();
    }
    
    public String getPass2ErrorDetail(){
        WebElement element = driver.findElement(By.xpath("//td[text()='New password again:']/..//span[@class='ui-message-error-detail']"));
        return element.getText();
    }
    
    public String getGlobalWarnMessage(){
        WebElement element = driver.findElement(By.xpath("//span[@class='ui-messages-warn-summary']"));
        return element.getText();
    }
    
    public String getGlobalInfoMessage(){
        WebElement element = driver.findElement(By.xpath("//span[@class='ui-messages-info-summary']"));
        return element.getText();
    }
}
