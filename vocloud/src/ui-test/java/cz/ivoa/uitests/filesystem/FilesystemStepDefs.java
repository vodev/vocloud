package cz.ivoa.uitests.filesystem;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.ivoa.pageobjects.IndexPage;
import cz.ivoa.pageobjects.LoggedIndexPage;
import cz.ivoa.pageobjects.LoginPage;
import cz.ivoa.pageobjects.ManageFilesystemPage;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.utils.DatabaseUtils;
import cz.ivoa.vocloud.utils.WebDriverProvider;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author radio.koza
 */
public class FilesystemStepDefs {

    private static final String managerUsername = "testGroupManager";
    private static final String managerPass = "testGroupManager123";

    @Before
    public void setup() {
        DatabaseUtils.createUser(managerUsername, managerPass, UserGroupName.MANAGER);
    }

    @After
    public void tearDown() {
        WebDriverProvider.closeCurrentDriver();
        DatabaseUtils.deleteUser(managerUsername);
    }

    private LoggedIndexPage loggedIndexPage;
    private ManageFilesystemPage manageFilesystemPage;
    
    @Given("^I am logged in as manager$")
    public void iAmLoggedInAsManager() throws Throwable {
        IndexPage index = new IndexPage(WebDriverProvider.getWebDriver());
        LoginPage loginPage = index.clickLoginMenuItem();
        loginPage.setUsername(managerUsername);
        loginPage.setPassword(managerPass);
        loginPage.login();
        loggedIndexPage = new LoggedIndexPage(WebDriverProvider.getWebDriver(), managerUsername);
    }

    @Given("^I am on Manage filesystem page$")
    public void iAmOnManageFilesystemPage() throws Throwable {
        manageFilesystemPage = loggedIndexPage.clickManageFilesystem();
    }

    @When("^I click Delete selected button$")
    public void iClickDeleteSelectedButton() throws Throwable {
        manageFilesystemPage.clickDeleteSelected();
    }

    @When("^I click Yes on confirmation dialog$")
    public void iClickYesOnConfirmationDialog() throws Throwable {
        manageFilesystemPage.clickConfirmButton();
    }

    @Then("^Filesystem warning message is shown (.+)$")
    public void globalWarningMessageIsShown(String message) throws Throwable {
        assertEquals(message, manageFilesystemPage.getGlobalWarnMessage());
    }

    @When("^I click New folder button$")
    public void iClickNewFolderButton() throws Throwable {
        manageFilesystemPage.clickNewFolderBtn();
    }

    @When("^I click Create folder button$")
    public void iClickCreateFolderButton() throws Throwable {
        manageFilesystemPage.clickCreateNewFolder();
    }

    @Then("^Filesystem error message is shown (.+)$")
    public void globalErrorMessageIsShown(String message) throws Throwable {
        assertEquals(message, manageFilesystemPage.getGlobalErrorMessage());
    }

    @When("^I type folder name (.+)$")
    public void iTypeFolderName(String folderName) throws Throwable {
        manageFilesystemPage.setNewFolderName(folderName);
    }

    @Then("^Filesystem info message is shown (.+)$")
    public void globalInfoMessageIsShown(String message) throws Throwable {
        assertEquals(message, manageFilesystemPage.getGlobalInfoMessage());
    }

    @Then("^Folder (.+) is listed$")
    public void folderIsListed(String folderName) throws Throwable {
        assertTrue(manageFilesystemPage.folderIsListed(folderName));
    }

    @When("^I check folder row (.+)$")
    public void iCheckFolderRow(String folderName) throws Throwable {
        manageFilesystemPage.checkRowWithFolder(folderName);
    }

    @Then("^Folder (.+) is not listed$")
    public void folderFolderForTestingPurposesIsNotListed(String folderName) throws Throwable {
        assertFalse(manageFilesystemPage.folderIsListed(folderName));
    }
}
