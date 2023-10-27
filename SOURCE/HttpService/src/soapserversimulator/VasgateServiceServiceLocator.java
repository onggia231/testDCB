/**
 * VasgateServiceServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package soapserversimulator;

public class VasgateServiceServiceLocator extends org.apache.axis.client.Service implements soapserversimulator.VasgateServiceService {

    public VasgateServiceServiceLocator() {
    }


    public VasgateServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public VasgateServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for VasgateService
    private java.lang.String VasgateService_address = "http://localhost:8080//services/com/telsoft/httpservice/soap/client/services/VasgateService";

    public java.lang.String getVasgateServiceAddress() {
        return VasgateService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String VasgateServiceWSDDServiceName = "VasgateService";

    public java.lang.String getVasgateServiceWSDDServiceName() {
        return VasgateServiceWSDDServiceName;
    }

    public void setVasgateServiceWSDDServiceName(java.lang.String name) {
        VasgateServiceWSDDServiceName = name;
    }

    public soapserversimulator.VasgateService_PortType getVasgateService() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(VasgateService_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getVasgateService(endpoint);
    }

    public soapserversimulator.VasgateService_PortType getVasgateService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            soapserversimulator.VasgateServiceSoapBindingStub _stub = new soapserversimulator.VasgateServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getVasgateServiceWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setVasgateServiceEndpointAddress(java.lang.String address) {
        VasgateService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (soapserversimulator.VasgateService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                soapserversimulator.VasgateServiceSoapBindingStub _stub = new soapserversimulator.VasgateServiceSoapBindingStub(new java.net.URL(VasgateService_address), this);
                _stub.setPortName(getVasgateServiceWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("VasgateService".equals(inputPortName)) {
            return getVasgateService();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.client.soap.httpservice.telsoft.com", "VasgateServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.client.soap.httpservice.telsoft.com", "VasgateService"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("VasgateService".equals(portName)) {
            setVasgateServiceEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
