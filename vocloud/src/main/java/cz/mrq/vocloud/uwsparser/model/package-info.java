@XmlSchema(
        namespace = "http://www.ivoa.net/xml/UWS/v1.0",
        elementFormDefault = XmlNsForm.QUALIFIED)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(value = UWSDateAdapter.class, type = Date.class)
})
@XmlAccessorType(XmlAccessType.FIELD)
package cz.mrq.vocloud.uwsparser.model;

import cz.mrq.vocloud.uwsparser.UWSDateAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.util.Date;
