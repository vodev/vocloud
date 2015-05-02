package cz.rk.vocloud.ssap.model;

import java.util.List;

/**
 *
 * @author radio.koza
 */
public class IndexedSSAPVotable {
    
    private static final String ACCREF_COLUMN_UTYPE = "ssa:access.reference";
    private static final String PUBDID_COLUMN_UTYPE = "ssa:curation.publisherdid";

    private List<Field> columnFields;
    private List<Record> rows;
    private String queryStatus = "undefined";//default value

    private Integer accrefIndex;
    private Integer pubidIndex;

    private boolean datalinkAvailable;

    private String datalinkResourceUrl;
    private List<Param> datalinkInputParams;

    public IndexedSSAPVotable(List<Field> columnNames, List<Record> rows) {
        this.columnFields = columnNames;
        this.rows = rows;
        //find columns for accref and possibly pubid
        int counter = 0;
        for (Field f : columnNames) {
            switch (f.getUtype().toLowerCase()){
                case ACCREF_COLUMN_UTYPE:
                    accrefIndex = counter;
                    break;
                case PUBDID_COLUMN_UTYPE:
                    pubidIndex = counter;
                    break;
            }
            //increase counter
            counter++;
        }
    }

    public void setupDatalink(String resourceUrl, List<Param> inputParams) {
        //check that pubdid column is specified and id param in datalink spec was found
        if (pubidIndex == null){
            return;//datalink not available
        }
        boolean successFlag = false;
        for (Param i: inputParams){
            if (i.isIdParam()){
                successFlag = true;
                break;
            }
        }
        if (!successFlag){
            //no id param in datalink spec
            return;
        }
        this.datalinkAvailable = true;
        this.datalinkResourceUrl = resourceUrl;
        this.datalinkInputParams = inputParams;
    }

    public String getQueryStatus() {
        return queryStatus;
    }

    public void setQueryStatus(String queryStatus) {
        this.queryStatus = queryStatus;
    }

    public List<Field> getColumnFields() {
        return columnFields;
    }

    public List<Record> getRows() {
        return rows;
    }

    public boolean isDatalinkAvailable() {
        return datalinkAvailable;
    }

    public String getDatalinkResourceUrl() {
        return datalinkResourceUrl;
    }

    public List<Param> getDatalinkInputParams() {
        return datalinkInputParams;
    }

    public String getAccrefColumn(Record record) {
        if (record == null) {
            throw new IllegalArgumentException("null Record passed as argument");
        }
        if (accrefIndex == null) {
            return null;
        }
        return record.getColumns().get(accrefIndex);
    }

    public String getPubDIDColumn(Record record) {
        if (record == null) {
            throw new IllegalArgumentException("null Record passed as argument");
        }
        if (pubidIndex == null) {
            return null;//pubdid column could not be defined
        }
        return record.getColumns().get(pubidIndex);
    }
}
