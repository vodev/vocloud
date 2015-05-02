package cz.rk.vocloud.ssap.model;

import java.util.List;

/**
 * This class represents one column (TR enclosing) of data in SSAP votable.
 * @author radio.koza
 */
public class Record {
    private final List<String> columns;
    
    public Record(List<String> columns){
        this.columns = columns;
    }

    @Override
    public String toString() {
        return "Record{" + "columns=" + columns + '}';
    }

    public List<String> getColumns() {
        return columns;
    }
    
    
}
