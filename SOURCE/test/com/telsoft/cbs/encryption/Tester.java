package com.telsoft.cbs.encryption;

import java.io.File;

public class Tester {

    private static final String PASSPHRASE = "mobifone";

    private static final String DE_INPUT = "x.pgp";
    private static final String DE_OUTPUT = "dx.txt";
    private static final String DE_KEY_FILE = "key/pgp/mobifone-priv.asc";

    private static final String E_INPUT = "x.txt";
    private static final String E_OUTPUT = "x.pgp";
    private static final String E_KEY_FILE = "key/pgp/mobifone-pub-sub.asc";


    public static void testDecrypt() throws Exception {
        PGPFileProcessor p = new PGPFileProcessor();
        p.setInputFileName(DE_INPUT);
        p.setOutputFileName(DE_OUTPUT);
        p.setPassphrase(PASSPHRASE);
        p.setSecretKeyFileName(DE_KEY_FILE);
        System.out.println(p.decrypt());
    }

    public static void testEncrypt() throws Exception {
        PGPFileProcessor p = new PGPFileProcessor();
        p.setInputFileName(E_INPUT);
        p.setOutputFileName(E_OUTPUT);
        p.setPassphrase(PASSPHRASE);
        p.setPublicKeyFileName(E_KEY_FILE);
        System.out.println(p.encrypt());
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new File(".").getAbsolutePath());
        String filename = "VASGATE_20210305020344.txt";
        String date = filename.substring(8, 22);
        System.out.println(date);
        long t = System.currentTimeMillis();
//        testEncrypt();
        System.out.println(System.currentTimeMillis() - t);
        t = System.currentTimeMillis();
        testDecrypt();
        System.out.println(System.currentTimeMillis() - t);
    }
}