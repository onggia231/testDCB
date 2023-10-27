package com.telsoft.httpservice.soap.client;

import lombok.Getter;
import lombok.Setter;

import java.net.URL;
import java.rmi.RemoteException;

public class SoapClient {
    @Getter
    @Setter
    private String host;

    public SoapClient() {
    }

    public int vasgateService(String path, String msisdn, String action) throws Exception {
        URL url = new URL(this.host + path);
        soapserversimulator.VasgateServiceSoapBindingStub binding;
        try {
            binding = (soapserversimulator.VasgateServiceSoapBindingStub)
                    new soapserversimulator.VasgateServiceServiceLocator().getVasgateService(url);
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        int result = -3;
        result = binding.vasgate(msisdn, action);
        // TBD - validate results
        return result;
    }

    public int antiProfit(String path, String msisdn) throws Exception {
        URL url = new URL(this.host + path);
        soapserversimulator.AntiProfitServiceSoapBindingStub binding;
        try {
            binding = (soapserversimulator.AntiProfitServiceSoapBindingStub)
                    new soapserversimulator.AntiProfitServiceServiceLocator().getAntiProfitService(url);
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        int result = -3;
        result = binding.antiProfit(msisdn);
        // TBD - validate results
        return result;
    }

    public static void main(String[] args) throws Exception {
        String host = "http://10.11.10.144:9029";
        URL url = new URL(host);
        soapserversimulator.AntiProfitServiceSoapBindingStub binding;
        try {
            binding = (soapserversimulator.AntiProfitServiceSoapBindingStub)
                    new soapserversimulator.AntiProfitServiceServiceLocator().getAntiProfitService(url);
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        int result = -3;
        result = binding.antiProfit("test");
        // TBD - validate results
        System.out.println(result);
    }
}
