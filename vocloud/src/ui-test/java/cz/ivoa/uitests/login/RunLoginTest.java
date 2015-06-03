
package cz.ivoa.uitests.login;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 *
 * @author radio.koza
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", tags = "@Login", snippets = SnippetType.CAMELCASE, features = "src/test/resources/cz/ivoa/uitests/login/")
public class RunLoginTest {
    
}
