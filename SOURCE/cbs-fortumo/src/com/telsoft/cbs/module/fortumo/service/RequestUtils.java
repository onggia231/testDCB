package com.telsoft.cbs.module.fortumo.service;

import com.telsoft.cbs.module.fortumo.domain.Request;
import com.telsoft.cbs.module.fortumo.domain.Response;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

public class RequestUtils {
    private static JAXBContext jaxbContext = null;

    static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("com.telsoft.cbs.module.fortumo.domain");
        }
        return jaxbContext;
    }

    public static Request createRequest(String xml) throws Exception {
        JAXBContext jaxbContext = getJAXBContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Request) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }

    public static Response createResponse(String xml) throws Exception {
        JAXBContext jaxbContext = getJAXBContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (Response) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }
}
