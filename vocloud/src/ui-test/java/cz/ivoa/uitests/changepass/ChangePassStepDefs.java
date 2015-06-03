package cz.ivoa.uitests.changepass;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cz.ivoa.pageobjects.ChangePassPage;
import cz.ivoa.pageobjects.IndexPage;
import cz.ivoa.pageobjects.LoggedIndexPage;
import cz.ivoa.pageobjects.LoginPage;
import cz.ivoa.vocloud.entity.UserGroupName;
import cz.ivoa.vocloud.utils.DatabaseUtils;
import cz.ivoa.vocloud.utils.WebDriverProvider;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author radio.koza
 */
public class ChangePassStepDefs {

    private static final String username = "testGroupUser";
    private static final String pass = "testGroupUser123";

    @Before
    public void setup() {
        DatabaseUtils.createUser(username, pass, UserGroupName.USER);
    }

    @After
    public void tearDown() {
        WebDriverProvider.closeCurrentDriver();
        DatabaseUtils.deleteUser(username);
    }

    private LoggedIndexPage loggedPage;
    private ChangePassPage changePassPage;
    private LoginPage loginPage;

    @Given("^I am logged in as testGroupUser$")
    public void iAmLoggedInAsUser() throws Throwable {
        IndexPage index = new IndexPage(WebDriverProvider.getWebDriver());
        index.clickLoginMenuItem();
        loginPage = new LoginPage(WebDriverProvider.getWebDriver());
        loginPage.setUsername(username);
        loginPage.setPassword(pass);
        loginPage.login();
        loggedPage = new LoggedIndexPage(WebDriverProvider.getWebDriver(), username);
    }

    @Given("^I am on change password page$")
    public void iAmOnChangePasswordPage() throws Throwable {
        changePassPage = loggedPage.clickChangePassMenuItem();
    }

    @When("^I click Submit button$")
    public void iClickSubmitButton() throws Throwable {
        changePassPage.clickSubmitButton();
    }

    @Then("^Old password error details show (.+)$")
    public void oldPasswordErrorDetails(String message) throws Throwable {
        assertEquals(message, changePassPage.getOldPassErrorDetail());
    }

    @Then("^New password error details show (.+)$")
    public void newPasswordErrorDetails(String message) throws Throwable {
        assertEquals(message, changePassPage.getPass1ErrorDetail());
    }

    @Then("^New password again error details show (.+)$")
    public void newPasswordAgainErrorDetails(String message) throws Throwable {
        assertEquals(message, changePassPage.getPass2ErrorDetail());
    }

    @When("^I type old password (.+)$")
    public void iTypeOldPasswordWrongPassword(String pass) throws Throwable {
        changePassPage.setOldPass(pass);
    }

    @When("^I type new password 1 (.+)$")
    public void iTypeNewPasswordNewPass(String pass) throws Throwable {
        changePassPage.setNewPass1(pass);
    }

    @When("^I type new password 2 (.+)$")
    public void iTypeNewPasswordNewPassAgain(String pass) throws Throwable {
        changePassPage.setNewPass2(pass);
    }

    @Then("^Global warning message is shown (.+)$")
    public void globalWarningMessageIsShown(String message) throws Throwable {
        assertEquals(message, changePassPage.getGlobalWarnMessage());
    }

    @Then("^Global info message is shown (.+)$")
    public void globalInfoMessageIsShown(String message) throws Throwable {
        assertEquals(message, changePassPage.getGlobalInfoMessage());
    }

    @When("^I click Logout$")
    public void iClickLogout() throws Throwable {
        changePassPage.logout();
        loginPage = new LoginPage(WebDriverProvider.getWebDriver());
    }

    @When("^I type username (.+)$")
    public void iTypeUsername(String username) throws Throwable {
        loginPage.setUsername(username);
    }

    @When("^I type password (.+)$")
    public void iTypePasswordNewPass(String pass) throws Throwable {
        loginPage.setPassword(pass);
    }

    @When("^I click Login button$")
    public void iClickLoginButton() throws Throwable {
        loginPage.login();
    }

    @Then("^I am logged in$")
    public void iAmLoggedIn() throws Throwable {
        loggedPage = new LoggedIndexPage(WebDriverProvider.getWebDriver(), username);
    }

}
