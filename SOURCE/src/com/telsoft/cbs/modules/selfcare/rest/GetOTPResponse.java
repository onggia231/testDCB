package com.telsoft.cbs.modules.selfcare.rest;

import com.telsoft.cbs.domain.CBCommand;
import com.telsoft.cbs.modules.rest.domain.Attribute;
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
public class GetOTPResponse implements Serializable {

    private static final long serialVersionUID = -95568649148078978L;
    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private String description;

    @Getter
    @Setter
    private String otp;

}
