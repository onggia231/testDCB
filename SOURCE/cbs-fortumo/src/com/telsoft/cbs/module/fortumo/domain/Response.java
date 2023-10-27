package com.telsoft.cbs.module.fortumo.domain;

import lombok.Data;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.Serializable;
import java.io.StringWriter;

@Data
public abstract class Response implements Serializable {
    private static final long serialVersionUID = -5603561497167857185L;
    private ResponseResult result = new ResponseResult();

    public static Response createResponse(Request request) {
        if (request != null)
            return request.createResponse();
        return null;
    }

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
