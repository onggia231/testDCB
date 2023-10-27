package com.telsoft.cbs.encryption;

public class DecryptThread extends EncryptionThread {
    @Override
    protected String getKeyParameterName() {
        return "privateKeyFilename";
    }

    @Override
    protected void process(PGPFileProcessor p) throws Exception {
        p.setSecretKeyFileName(getKeyFileName());
        p.decrypt();
    }

    @Override
    protected void beforeProcessFile(String inputFilename) {
        logMonitor("Start decrypting file " + inputFilename);
    }

    @Override
    protected void afterProcessFile(String inputFilename) {
        logMonitor("End decrypting file " + inputFilename);
    }

    @Override
    protected void logProcessed(String inputFilename, String outputFilename, boolean success, long duration, String reason) {
        logMonitor("Decrypt file " + inputFilename + " is " + (success ? "success" : ("failed:" + reason)) + ":" + duration + "ms");
    }
}
