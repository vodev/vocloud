package cz.ivoa.pageobjects;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

/**
 *
 * @author radio.koza
 */
public class ManageFilesystemPage extends LogoutablePage {

    private static final Logger LOG = Logger.getLogger(ManageFilesystemPage.class.getName());

    @FindBy(xpath = "//a[@id='form:newFolderBtn']")
    private WebElement newFolderButton;

    @FindBy(xpath = "//a[@id='form:deleteSelectedBtn']")
    private WebElement deleteSelectedButton;

    public ManageFilesystemPage(WebDriver driver, String username) {
        super(driver, username);
        waitForObject(By.xpath("//a[@id='form:newFolderBtn']"));
        PageFactory.initElements(driver, this);
    }

    public ManageFilesystemPage clickNewFolderBtn() {
        newFolderButton.click();
        return this;
    }

    public ManageFilesystemPage setNewFolderName(String folderName) {
        LOG.log(Level.INFO, "Setting folder name to: {0}", folderName);
        int retries = 5;
        do {
            try {
                By input = By.xpath("//div[@class='ui-overlaypanel-content']//input");
                waitForObject(input);
                driver.findElement(input).sendKeys(folderName);
                break;
            } catch (StaleElementReferenceException ex) {
                LOG.info("Stale element exception - looping again;");
                retries--;
                try {
                    Thread.sleep(200);//the timeout is necessary to ensure ajax text field is properly loaded
                } catch (InterruptedException ex2) {
                    LOG.log(Level.INFO, null, ex2);
                }
            }
        } while (retries > 0);
        return this;
    }

    public ManageFilesystemPage clickCreateNewFolder() {
        LOG.info("Clicking Create folder button");
        int retries = 5;
        do {
            try {
                By button = By.xpath("//div[@class='ui-overlaypanel-content']//button");
                waitForObject(button);
                driver.findElement(button).click();
                break;
            } catch (StaleElementReferenceException ex) {
                LOG.info("Stale element exception - looping again");
                retries--;
            }
        } while (retries > 0);

        return this;
    }

    public String getGlobalErrorMessage() {
        By message = By.xpath("//span[@class='ui-messages-error-detail']");
        waitForObject(message);
        return driver.findElement(message).getText();
    }

    public String getGlobalInfoMessage() {
        By message = By.xpath("//span[@class='ui-messages-info-detail']");
        waitForObject(message);
        return driver.findElement(message).getText();
    }

    public String getGlobalWarnMessage() {
        By message = By.xpath("//span[@class='ui-messages-warn-detail']");
        waitForObject(message);
        return driver.findElement(message).getText();
    }

    public boolean folderIsListed(String folderName) {
        By folderLink = By.xpath("//tr[@data-rk='" + folderName + "']/td/a");
        try {
            driver.findElement(folderLink);
        } catch (NoSuchElementException ex) {
            return false;
        }
        return true;
    }

    public ManageFilesystemPage clickDeleteSelected() {
        LOG.info("Clicking Delete selected button");
        deleteSelectedButton.click();
        return this;
    }

    public ManageFilesystemPage checkRowWithFolder(String folderName) {
        LOG.log(Level.INFO, "Clicking on checkbox of folder row: {0}", folderName);
        By row = By.xpath("//tr[@data-rk='" + folderName + "']");
        waitForObject(row);
        driver.findElement(row).click();
        return this;
    }

    public ManageFilesystemPage clickConfirmButton() {
        LOG.log(Level.INFO, "Clicking confirm button on confirmation dialog");
        By button = By.xpath("//button[@id='form:confirm']");
        waitForObject(button);
        driver.findElement(button).click();
        return this;
    }

    public ManageFilesystemPage clickDeclineButton() {
        LOG.log(Level.INFO, "Clicking decline button on confirmation dialog");
        By button = By.xpath("//button[@id='form:decline']");
        waitForObject(button);
        driver.findElement(button).click();
        return this;
    }
}
