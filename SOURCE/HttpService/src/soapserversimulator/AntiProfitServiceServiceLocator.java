/**
 * AntiProfitServiceServiceLocator.java
 * <p>
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package soapserversimulator;

public class AntiProfitServiceServiceLocator extends org.apache.axis.client.Service implements soapserversimulator.AntiProfitServiceService {

    public AntiProfitServiceServiceLocator() {
    }


    public AntiProfitServiceServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public AntiProfitServiceServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for AntiProfitService
    private java.lang.String AntiProfitService_address = "http://localhost:8080//services/com/telsoft/httpservice/soap/client/services/AntiProfitService";

    public java.lang.String getAntiProfitServiceAddress() {
        return AntiProfitService_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String AntiProfitServiceWSDDServiceName = "AntiProfitService";

    public java.lang.String getAntiProfitServiceWSDDServiceName() {
        return AntiProfitServiceWSDDServiceName;
    }

    public void setAntiProfitServiceWSDDServiceName(java.lang.String name) {
        AntiProfitServiceWSDDServiceName = name;
    }

    public soapserversimulator.AntiProfitService_PortType getAntiProfitService() throws javax.xml.rpc.ServiceException {
        java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(AntiProfitService_address);
        } catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getAntiProfitService(endpoint);
    }

    public soapserversimulator.AntiProfitService_PortType getAntiProfitService(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            soapserversimulator.AntiProfitServiceSoapBindingStub _stub = new soapserversimulator.AntiProfitServiceSoapBindingStub(portAddress, this);
            _stub.setPortName(getAntiProfitServiceWSDDServiceName());
            return _stub;
        } catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setAntiProfitServiceEndpointAddress(java.lang.String address) {
        AntiProfitService_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (soapserversimulator.AntiProfitService_PortType.class.isAssignableFrom(serviceEndpointInterface)) {
                soapserversimulator.AntiProfitServiceSoapBindingStub _stub = new soapserversimulator.AntiProfitServiceSoapBindingStub(new java.net.URL(AntiProfitService_address), this);
                _stub.setPortName(getAntiProfitServiceWSDDServiceName());
                return _stub;
            }
        } catch (java.lang.Throwable t) {
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
        if ("AntiProfitService".equals(inputPortName)) {
            return getAntiProfitService();
        } else {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://services.client.soap.httpservice.telsoft.com", "AntiProfitServiceService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://services.client.soap.httpservice.telsoft.com", "AntiProfitService"));
        }
        return ports.iterator();
    }

    /**
     * Set the endpoint address for the specified port name.
     */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {

        if ("AntiProfitService".equals(portName)) {
            setAntiProfitServiceEndpointAddress(address);
        } else { // Unknown Port Name
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
