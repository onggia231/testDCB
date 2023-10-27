package com.telsoft.cbs.encryption;

import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;

import java.util.Vector;

public abstract class EncryptionThread extends FileProcessingThread {
    private String passphrase;
    private String keyFileName;

    public String getPassphrase() {
        return passphrase;
    }

    public void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    public String getKeyFileName() {
        return keyFileName;
    }

    public void setKeyFileName(String keyFileName) {
        this.keyFileName = keyFileName;
    }

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("Passphrase", "", ParameterType.PARAM_PASSWORD, "99"));
        vtReturn.add(createParameter(getKeyParameterName(), "", ParameterType.PARAM_TEXTBOX_MAX, "256"));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    protected abstract String getKeyParameterName();

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        passphrase = loadString("Passphrase");
        keyFileName = loadString(getKeyParameterName());
    }

    @Override
    protected void processFile(String inputFilename, String outputFilename) throws Exception {
        PGPFileProcessor p = new PGPFileProcessor();
        p.setInputFileName(inputFilename);
        p.setOutputFileName(outputFilename);
        p.setPassphrase(getPassphrase());
        process(p);
    }

    protected abstract void process(PGPFileProcessor p) throws Exception;
}
