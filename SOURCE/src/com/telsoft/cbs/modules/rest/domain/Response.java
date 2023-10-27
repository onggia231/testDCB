package com.telsoft.cbs.modules.rest.domain;

import com.telsoft.cbs.domain.CBCommand;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class Response implements Serializable {
    private static final long serialVersionUID = -4800520425732242783L;
    @Getter
    @Setter
    @XmlElement(name = "parameter")
    private List<Attribute> parameters = new ArrayList<>();

    @Getter
    @Setter
    private CBCommand name;

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String isdn;

    @Getter
    @Setter
    private String transaction_id;

    @Getter
    @Setter
    private String source;
}
