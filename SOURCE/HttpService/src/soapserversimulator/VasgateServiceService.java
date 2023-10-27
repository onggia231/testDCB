/**
 * VasgateServiceService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package soapserversimulator;

public interface VasgateServiceService extends javax.xml.rpc.Service {
    public java.lang.String getVasgateServiceAddress();

    public soapserversimulator.VasgateService_PortType getVasgateService() throws javax.xml.rpc.ServiceException;

    public soapserversimulator.VasgateService_PortType getVasgateService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
