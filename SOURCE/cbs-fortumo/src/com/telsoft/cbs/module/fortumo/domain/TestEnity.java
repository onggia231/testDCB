package com.telsoft.cbs.module.fortumo.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@Getter
@Setter
@XmlRootElement(name = "TestEnity")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestEnity {
    private String testString;

    public TestEnity(String str) {
        this.testString = str;
    }

    public TestEnity getTestEnt() {
        return new TestEnity("tuanla");
    }

    public TestEnity setTestEntity(String str) {
        this.testString = str;
        return new TestEnity(str);
    }
}
