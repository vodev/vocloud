package cz.rk.vocloud.ssap.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author radio.koza
 */
public class Param {
    private final String name;
    private final String value;

    private final List<Option> options;

    private boolean isIdParam = false;
    
    public Param(String name, String value) {
        this.name = name;
        this.value = value;
        this.options = new ArrayList<>();
    }
    
    public void addOption(Option option){
        this.options.add(option);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public List<Option> getOptions() {
        return options;
    }
    
    public void setIdParam(){
        this.isIdParam = true;
    }
    
    public boolean isIdParam(){
        return this.isIdParam;
    }
    
}
