package cz.mrq.vocloud.uwsparser;

import cz.mrq.vocloud.uwsparser.model.UWSJob;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Lumir Mrkva (lumir.mrkva@topmonks.com)
 */
public class UWSDateAdapter extends XmlAdapter<String, Date> {

    @Override
    public String marshal(Date v) throws Exception {
        return new SimpleDateFormat(UWSJob.DATE_FORMAT).format(v);
    }

    @Override
    public Date unmarshal(String v) throws Exception {
        return new SimpleDateFormat(UWSJob.DATE_FORMAT).parse(v);
    }

}
