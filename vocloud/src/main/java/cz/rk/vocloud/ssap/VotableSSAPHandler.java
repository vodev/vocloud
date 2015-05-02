package cz.rk.vocloud.ssap;

import cz.rk.vocloud.ssap.model.Field;
import cz.rk.vocloud.ssap.model.IndexedSSAPVotable;
import cz.rk.vocloud.ssap.model.Option;
import cz.rk.vocloud.ssap.model.Param;
import cz.rk.vocloud.ssap.model.Record;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author radio.koza
 */
public class VotableSSAPHandler extends DefaultHandler {

    private boolean isResultResourceElement = false;
    private boolean insideTD = false;

    private final List<Field> resultFields;
    private final List<Record> resultRecords;

    private List<String> columns;
    private StringBuilder columnData;

    private String queryStatus;

    private boolean loadingNextResource;
    private boolean loadingInputParamGroup;

    private final List<PossibleDatalinkSpec> possibleDatalinks;

    private PossibleDatalinkSpec loadingDatalinkSpec;
    private Param loadingParam;

    public VotableSSAPHandler() {
        resultFields = new ArrayList<>();
        resultRecords = new ArrayList<>();
        possibleDatalinks = new ArrayList<>();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //checking resource element
        if ("RESOURCE".equals(qName)) {
            if ("results".equals(attributes.getValue("type"))) {
                isResultResourceElement = true;
            } else {
                loadingNextResource = true;
            }
        }
        //checking info tag with query status
        if (isResultResourceElement && "INFO".equals(qName) && "QUERY_STATUS".equals(attributes.getValue("name"))) {
            queryStatus = attributes.getValue("value");
        }
        //checking field element
        if (isResultResourceElement && "FIELD".equals(qName)) {
            String name = attributes.getValue("name");
            String utype = attributes.getValue("utype");
            if (name == null) {
                name = "undefined";
            }
            resultFields.add(new Field(name, utype));
        }
        //checking on column
        if (isResultResourceElement && "TR".equals(qName)) {
            columns = new ArrayList<>();
        }
        //parsing column properties
        if (isResultResourceElement && "TD".equals(qName)) {
            insideTD = true;
        }
        //check input param group element
        if (loadingNextResource && "GROUP".equals(qName) && "inputParams".equals(attributes.getValue("name"))) {
            loadingInputParamGroup = true;
        }
        //loading params if in additional resource elements
        if (loadingNextResource && "PARAM".equals(qName)) {
            if (loadingDatalinkSpec == null) {
                loadingDatalinkSpec = new PossibleDatalinkSpec();
            }
            String name = attributes.getValue("name");
            String value = attributes.getValue("value");
            if (name != null && value != null) {
                loadingParam = new Param(name, value);
            }
            //check if it is id param
            if (loadingInputParamGroup && loadingParam != null && "ssa_pubDID".equals(attributes.getValue("ref"))) {//TODO will have to improve later
                loadingParam.setIdParam();
            }
        }
        //option inside param
        if (loadingInputParamGroup && loadingParam != null && "OPTION".equals(qName)) {
            String name = attributes.getValue("name");
            String value = attributes.getValue("value");
            if (name != null && value != null) {
                loadingParam.addOption(new Option(name, value));
            }
        }

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        //checking resource element
        if ("RESOURCE".equals(qName)) {
            if (isResultResourceElement) {
                isResultResourceElement = false;
            } else {
                loadingNextResource = false;
                if (loadingDatalinkSpec != null) {
                    if (loadingDatalinkSpec.hasProperFormat()) {
                        possibleDatalinks.add(loadingDatalinkSpec);
                    }
                    loadingDatalinkSpec = null;
                }
            }
        }
        //end of column in resource element
        if (columns != null && "TR".equals(qName)) {
            resultRecords.add(new Record(columns));
            columns = null;
        }
        //end of parsing column
        if (insideTD && "TD".equals(qName)) {
            insideTD = false;
            if (columnData != null) {
                columns.add(columnData.toString().trim());
                columnData = null;
            } else {
                columns.add("");
            }
        }
        //check end of input param group
        if (loadingInputParamGroup && "GROUP".equals(qName)) {
            loadingInputParamGroup = false;
        }
        //check param
        if (loadingNextResource && "PARAM".equals(qName)) {
            if (loadingInputParamGroup) {
                loadingDatalinkSpec.addInputParam(loadingParam);
            } else {
                loadingDatalinkSpec.addExternalParam(loadingParam);
            }
            loadingParam = null;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (insideTD) {
            String data = new String(ch, start, length);//ignoring white chars
            if (columnData == null) {
                columnData = new StringBuilder(data);
            } else {
                columnData.append(data);
            }
        }
    }
    /*
     //unnecessary methods
     public List<Record> getRecords() {
     return this.resultRecords;
     }

     public int getRecordCount() {
     return this.resultRecords.size();
     }

     public List<String> getFields() {
     return this.resultFields;
     }

     public String getQueryStatus() {
     return this.queryStatus;
     }
     */

    private static class PossibleDatalinkSpec {

        private final List<Param> inputParams;
        private final Map<String, Param> externalParams;

        public PossibleDatalinkSpec() {
            this.inputParams = new ArrayList<>();
            this.externalParams = new HashMap<>();
        }

        public void addInputParam(Param param) {
            this.inputParams.add(param);
        }

        public void addExternalParam(Param param) {
            this.externalParams.put(param.getName(), param);
        }

        public List<Param> getInputParams() {
            return inputParams;
        }

        public Map<String, Param> getExternalParams() {
            return externalParams;
        }

        public boolean hasProperFormat() {
            return externalParams.containsKey("accessURL") && !inputParams.isEmpty();
        }

        public String getAccessUrl() {
            return externalParams.get("accessURL").getValue();
        }
    }

    public IndexedSSAPVotable buildIndexedVotable() {
        IndexedSSAPVotable votable = new IndexedSSAPVotable(resultFields, resultRecords);
        votable.setQueryStatus(queryStatus);
        //choose proper datalink service if any
        PossibleDatalinkSpec theBest = null;
        for (PossibleDatalinkSpec i : possibleDatalinks) {
            if (theBest == null) {
                theBest = i;
                continue;
            }
            if (i.inputParams.size() > theBest.inputParams.size()) {
                theBest = i;//not the best way but it could work
            }
        }
        if (theBest == null) {
            return votable;//datalink is not available
        }
        //else
        votable.setupDatalink(theBest.getAccessUrl(), theBest.getInputParams());
        return votable;
    }
}
