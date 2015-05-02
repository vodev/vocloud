package cz.mrq.vocloud.uwsparser;

import cz.mrq.vocloud.uwsparser.model.UWSJob;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
public class UWSParserTest {

    String xml;

    UWSParser parser = UWSParserManager.getInstance().getParser();

    @Before
    public void setUp() throws Exception {
        xml = readResource("pending.xml");
    }

    public UWSJob parseJob(String xml) throws Exception {
        UWSJob job = parser.parseJob(xml);
        Assert.assertNotNull(job);
        Assert.assertNotNull(job.getJobId());
        return job;
    }

    @Test
    public void testParserBasic() throws Exception {
        parseJob(xml);
    }

    @Test
    public void testParserSimple() throws Exception {
        List<String> xmls = new ArrayList<>();
        xmls.add(xml);
        xmls.add(readResource("executing.xml"));
        xmls.add(readResource("completed.xml"));
        xmls.add(readResource("error.xml"));

        for (String xml : xmls) {
            System.out.println(parseJob(xml));
        }
    }

    @Test
    public void testParserParameters() throws Exception {
        UWSJob job = parseJob(xml);

        Assert.assertEquals("different number of params", 1, job.getParameters().size());
        Assert.assertEquals("not expected param id", "zip", job.getParameters().get(0).getId());
        Assert.assertEquals("not expected param", "http://vocloud/vokorel/download/8f3f3f95-cad6-4877-b159-42663d6b7686/parameters.zip", job.getParameters().get(0).getValue());
    }

    @Test
    public void testParserResults() throws Exception {
        String xml = readResource("completed.xml");

        UWSJob job = parseJob(xml);

        Assert.assertEquals("different number of results", 1, job.getResults().size());
        Assert.assertEquals("not expected result url", "http://192.168.192.118/uws-korel/results/korel/1394313702207A/results.zip", job.getResultUrl());
    }

    @Test
    public void testParserErrorSummary() throws Exception {
        UWSJob job = parseJob(readResource("error.xml"));

        Assert.assertNotNull("error not parsed", job.getErrorSummary());
        Assert.assertEquals("different type", "FATAL", job.getErrorSummary().getType());
        Assert.assertEquals("different message", "Cannot download ZIP file.", job.getErrorSummary().getMessage());

        job = parseJob(readResource("completed.xml"));
        Assert.assertNull("error should be null", job.getErrorSummary());
    }

    static String readResource(String resource)
            throws IOException, URISyntaxException {
        byte[] encoded = Files.readAllBytes(Paths.get(UWSParserTest.class.getResource(resource).toURI()));
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(encoded)).toString();
    }

}
