package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "SubmitMOResponseDTO")
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmitMOResponseDTO {
    private ResultStatus status;
    private Integer code;
    private String description;

//    @Override
//    public String toString() {
//        return "SubmitMOResponseDTO{" +
//                "status=" + status +
//                ", code=" + code +
//                ", description='" + description + '\'' +
//                '}';
//    }
}
