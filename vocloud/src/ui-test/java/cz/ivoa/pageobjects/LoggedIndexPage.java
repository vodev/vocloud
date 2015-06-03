package cz.ivoa.pageobjects;

import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 *
 * @author radio.koza
 */
public class LoggedIndexPage extends LogoutablePage {

    private static final Logger LOG = Logger.getLogger(LoggedIndexPage.class.getName());

    private WebElement showFilesystemMenuItem;
    private boolean isManagerLogged;

    @FindBy(xpath = "//span[text()='Settings']/../../a")
    private WebElement changePassMenuItem;
    
    public LoggedIndexPage(WebDriver driver, String username) {
        super(driver, username);
        //if user has manager or admin role, the manage filesystem button is shown in menu otherwise the view filesystem button is shown
        try {
            showFilesystemMenuItem = driver.findElement(By.xpath("//span[text()='Manage filesystem']/../../a"));
            isManagerLogged = true;//manager or admin
        } catch (NoSuchElementException ex) {
            isManagerLogged = false;//common user is logged
            showFilesystemMenuItem = driver.findElement(By.xpath("//span[text()='View filesystem']/../../a"));
        }
        PageFactory.initElements(driver, this);
    }

    public boolean isManagerOrAdminLogged(){
        return isManagerLogged;
    }

    public ManageFilesystemPage clickManageFilesystem(){
        if (!isManagerLogged){
            throw new IllegalStateException("Nor manager nor admin is logged in");
        }
        LOG.info("Clicking Manage filesystem menu item");
        //recreation is necessary
        showFilesystemMenuItem = driver.findElement(By.xpath("//span[text()='Manage filesystem']/../../a"));
        showFilesystemMenuItem.click();
        return new ManageFilesystemPage(driver, username);
    }
    
    public ViewFilesystemPage clickViewFilesystem(){
        if (isManagerLogged){
            throw new IllegalStateException("Common user is not logged in to view filesystem in readonly mode");
        }
        LOG.info("Clicking View filesystem menu item");
        //recreation is necessary 
        showFilesystemMenuItem = driver.findElement(By.xpath("//span[text()='View filesystem']/../../a"));
        showFilesystemMenuItem.click();
        return new ViewFilesystemPage(driver, username);
    }
    
    public ChangePassPage clickChangePassMenuItem(){
        LOG.info("Clicking Change password menu item");
        changePassMenuItem.click();
        return new ChangePassPage(driver, username);
    }
}
