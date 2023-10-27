package com.telsoft.cbs.module.fortumo.domain;

import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class UserAccount implements Serializable {
    private static final long serialVersionUID = -4011945082860427163L;

    @XmlAttribute
    public RefType refType;

    @XmlValue
    protected String account;

    public UserAccount() {

    }

    public UserAccount(String account, RefType refType) {
        this.refType = refType;
        this.account = account;

    }

    @Override
    public String toString() {
        return "UserAccount{" +
                "refType=" + refType +
                ", account='" + account + '\'' +
                '}';
    }
}
