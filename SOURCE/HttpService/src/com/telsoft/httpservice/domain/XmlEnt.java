package com.telsoft.httpservice.domain;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.io.StringWriter;

public class XmlEnt implements Serializable {
	private static final long serialVersionUID = 3637110486216366659L;

	@Override
	public String toString() {
		try
		{
			//Create JAXB Context
			JAXBContext jaxbContext = JAXBContext.newInstance(this.getClass());

			//Create Marshaller
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			//Required formatting??
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

			//Print XML String to Console
			StringWriter sw = new StringWriter();

			//Write XML to StringWriter
			jaxbMarshaller.marshal(this, sw);

			//Verify XML Content
			String xmlContent = sw.toString();
			return xmlContent;

		} catch (JAXBException e) {
			e.printStackTrace();
			return super.toString();
		}
	}
}
