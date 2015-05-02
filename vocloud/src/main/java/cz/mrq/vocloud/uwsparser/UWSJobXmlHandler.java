package cz.mrq.vocloud.uwsparser;

import cz.mrq.vocloud.entity.Phase;
import cz.mrq.vocloud.uwsparser.model.ErrorSummary;
import cz.mrq.vocloud.uwsparser.model.UWSJob;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author voadmin
 */
@Deprecated
public class UWSJobXmlHandler extends DefaultHandler {

    private UWSJob job = new UWSJob();

    public UWSJob getJob() {
        return job;
    }
    private Elements current = Elements.UNKNOWN;

    private List<String> results = new ArrayList<>();

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        current = Elements.getElement(localName);

        if ("true".equals(attributes.getValue("xsi:nil"))) {
            current = Elements.UNKNOWN;
        }

        if (current == Elements.result) {
            String result = attributes.getValue(2);
            if (result == null) {
                // needed if job finishes with error
                result = attributes.getValue(0);
                results.add(result);
            }
            // not updated since UWSJob object refactor
            //job.setResults(results);

            current = Elements.UNKNOWN;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (current == Elements.UNKNOWN) {
            return;
        }

        String string = new String(ch, start, length);

        Logger.getLogger(UWSParser.class.getName()).log(Level.WARNING, current.toString() + ": " + string);
        switch (current) {
            case jobId:
                job.setJobId(string);
                break;
            case runId:
                job.setRunId(string);
                break;
            case ownerId:
                job.setOwner(string);
                break;
            case phase:
                job.setPhase(Phase.getPhase(string));
                break;
            case startTime:
                job.setStartTime(string);
                break;
            case endTime:
                job.setEndTime(string);
                break;
            case executionDuration:
                job.setExecutionDuration(Long.parseLong(string));
                break;
            case destruction:
                job.setDestruction(string);
                break;
            case message:
                ErrorSummary error = new ErrorSummary();
                error.setMessage(string);
                job.setErrorSummary(error);
                break;
            default:
                break;
        }
        current = Elements.UNKNOWN;
    }

    private enum Elements {

        jobId, runId, ownerId, phase, quote, startTime, endTime, executionDuration, destruction, results, parameters, parameter, job, result, errorSummary, message, UNKNOWN;

        public static Elements getElement(String phStr) {
            try {
                return valueOf(phStr);
            } catch (Exception ex) {
                return Elements.UNKNOWN;
            }

        }
    }
}
