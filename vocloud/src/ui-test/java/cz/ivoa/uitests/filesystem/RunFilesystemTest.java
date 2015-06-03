package cz.ivoa.uitests.filesystem;

import cucumber.api.CucumberOptions;
import cucumber.api.SnippetType;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

/**
 *
 * @author radio.koza
 */
@RunWith(Cucumber.class)
@CucumberOptions(plugin = "pretty", tags = "@Filesystem", 
        snippets = SnippetType.CAMELCASE, features = "src/test/resources/cz/ivoa/uitests/filesystem/")
public class RunFilesystemTest {
    
}
