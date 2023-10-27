package com.telsoft.cbs.module.fortumo.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseResult implements Serializable {
    private static final long serialVersionUID = 3765983104008416227L;
    @XmlAttribute
    private ResultStatus status;

    private Integer reasonCode;
    private String message;
    private Boolean retriable;
    private Integer retryDelay;
}
