package com.telsoft.cbs.module.cbsrest.server.service;

import com.telsoft.cbs.module.cbsrest.domain.RestRequest;
import com.telsoft.cbs.module.cbsrest.domain.RestResponse;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;

public class RequestUtils {
    private static JAXBContext jaxbContext = null;

    static JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            jaxbContext = JAXBContext.newInstance("com.telsoft.cbs.module.boku.domain");
        }
        return jaxbContext;
    }

    public static RestRequest createRequest(String xml) throws Exception {
        JAXBContext jaxbContext = getJAXBContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (RestRequest) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }

    public static RestResponse createResponse(String xml) throws Exception {
        JAXBContext jaxbContext = getJAXBContext();
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        return (RestResponse) unmarshaller.unmarshal(new ByteArrayInputStream(xml.getBytes()));
    }
}
