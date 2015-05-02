package cz.mrq.vocloud.uwsparser;

import cz.mrq.vocloud.uwsparser.model.UWSJob;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voadmin
 */
public class UWSParser {

    private static final Logger logger = Logger.getLogger(UWSParser.class.getName());

    public UWSJob parseJob(String xml) {
        if (xml == null) {
            logger.log(Level.SEVERE, "No XML to parse");
            return null;
        }
        try {
            return parseJobUsingJAXB(xml);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "cant parse uwsJob", e);
            return null;
        }
    }

    UWSJob parseJobUsingJAXB(String xml) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(UWSJob.class);

        Unmarshaller unmarshaller = jc.createUnmarshaller();
        UWSJob uwsJob = (UWSJob) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));

//        //debugging reasons only
//        Marshaller marshaller = jc.createMarshaller();
//        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//        marshaller.marshal(uwsJob, System.out);
        return uwsJob;
    }

    @Deprecated
    UWSJob parseJobUsingSax(String xml) {
        UWSJobXmlHandler handler = new UWSJobXmlHandler();
        try {
            XMLReader saxParser = XMLReaderFactory.createXMLReader();
            saxParser.setContentHandler(handler);
            saxParser.setErrorHandler(handler);
            saxParser.parse(xml);
        } catch (IOException | SAXException ex) {
            logger.log(Level.WARNING, "error when parsing \n" + xml);
            logger.log(Level.WARNING, null, ex);
        }

        return handler.getJob();
    }
}
