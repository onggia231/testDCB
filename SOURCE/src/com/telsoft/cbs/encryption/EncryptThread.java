package com.telsoft.cbs.encryption;

public class EncryptThread extends EncryptionThread {
    @Override
    protected String getKeyParameterName() {
        return "publicKeyFilename";
    }

    @Override
    protected void process(PGPFileProcessor p) throws Exception {
        p.setPublicKeyFileName(getKeyFileName());
        p.encrypt();
    }

    @Override
    protected void beforeProcessFile(String inputFilename) {
        logMonitor("Start encrypting file " + inputFilename);
    }

    @Override
    protected void afterProcessFile(String inputFilename) {
        logMonitor("End encrypting file " + inputFilename);
    }

    @Override
    protected void logProcessed(String inputFilename, String outputFilename, boolean success, long duration, String reason) {
        logMonitor("Encrypt file " + inputFilename + " is " + (success ? "success" : ("failed:" + reason)) + ":" + duration + "ms");
    }
}
