package cz.ivoa.uitests.login;

import cucumber.api.PendingException;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.ivoa.pageobjects.IndexPage;
import cz.ivoa.pageobjects.LoggedIndexPage;
import cz.ivoa.pageobjects.LoginErrorPage;
import cz.ivoa.pageobjects.LoginPage;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.utils.DatabaseUtils;
import cz.ivoa.vocloud.utils.WebDriverProvider;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author radio.koza
 */
public class LoginStepDefs {

    private static final String userName = "testGroupUser";
    private static final String managerName = "testGroupManager";
    private static final String adminName = "testGroupAdmin";

    private static final String userPass = "testGroupUser123";
    private static final String managerPass = "testGroupManager123";
    private static final String adminPass = "testGroupAdmin123";

    @Before
    public void setup() {
        DatabaseUtils.createUser(userName, userPass, UserGroupName.USER);
        DatabaseUtils.createUser(managerName, managerPass, UserGroupName.MANAGER);
        DatabaseUtils.createUser(adminName, adminPass, UserGroupName.ADMIN);
    }

    @After
    public void tearDown() {
        WebDriverProvider.closeCurrentDriver();
        DatabaseUtils.deleteUser(userName);
        DatabaseUtils.deleteUser(managerName);
        DatabaseUtils.deleteUser(adminName);
    }
    
    private LoginPage loginPage;
    private LoggedIndexPage loggedIndexPage;
    private String typedUsername;
    private String typedPass;

    @Given("^I am on login page$")
    public void iAmOnLoginPage() throws Throwable {
        IndexPage page = new IndexPage(WebDriverProvider.getWebDriver());
        page.clickLoginMenuItem();
        loginPage = new LoginPage(WebDriverProvider.getWebDriver());
    }

    @When("^I type username (.+)$")
    public void iTypeUsername(String username) throws Throwable {
        loginPage.setUsername(username);
        typedUsername = username;
    }

    @When("^I type password (.+)$")
    public void iTypePassword(String pass) throws Throwable {
        loginPage.setPassword(pass);
        typedPass = pass;
    }

    @When("^I click login button$")
    public void iClickLoginButton() throws Throwable {
        loginPage.login();
    }

    @Then("^Login error page is shown$")
    public void loginErrorPageIsShown() throws Throwable {
        LoginErrorPage errorPage = new LoginErrorPage(WebDriverProvider.getWebDriver());
        //throws exception if not on login error page
    }

    @Then("^I am logged in$")
    public void iAmLoggedIn() throws Throwable {
        if (loggedIndexPage == null){
            loggedIndexPage = new LoggedIndexPage(WebDriverProvider.getWebDriver(), typedUsername);
            //throws exception if not logged in
        }
    }

    @Then("^View filesystem button is visible$")
    public void viewFilesystemButtonIsVisible() throws Throwable {
        assertFalse(loggedIndexPage.isManagerOrAdminLogged());
    }

    @Then("^Manage filesystem button is visible$")
    public void manageFilesystemButtonIsVisible() throws Throwable {
        assertTrue(loggedIndexPage.isManagerOrAdminLogged());
    }

}
