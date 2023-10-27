package com.telsoft.cbs.module.fortumo.domain;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.io.StringWriter;

public abstract class Request implements Serializable {
    private static final long serialVersionUID = 3419070759431121496L;

    public abstract <T extends Response> T createResponse();

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
