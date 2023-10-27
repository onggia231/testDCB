package com.telsoft.cbs.modules.rest.domain;

import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.io.Serializable;

@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class Attribute implements Serializable {
	private static final long serialVersionUID = 1408651413214924768L;
	@XmlAttribute
	String key;

	@XmlValue
	String value;

	private Attribute() {
	}

	public Attribute(String key, String value) {
		this.key = key;
		this.value = value;
	}
}
