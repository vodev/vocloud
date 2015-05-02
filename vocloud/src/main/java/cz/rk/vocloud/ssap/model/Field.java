package cz.rk.vocloud.ssap.model;

/**
 *
 * @author radio.koza
 */
public class Field {
    private final String name;
    private final String utype;

    public Field(String name, String utype) {
        this.name = name;
        if (utype == null){
            this.utype = "";
        } else {
            this.utype = utype;
        }
    }

    public String getName() {
        return name;
    }

    public String getUtype() {
        return utype;
    }
    
}
