package com.telsoft.cbs.module.fortumo;

import com.telsoft.thread.DefaultManager;
import com.telsoft.thread.FileThreadLister2;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FortumoApp extends DefaultManager {
    private static final Logger log = LoggerFactory.getLogger(FortumoApp.class);

    public FortumoApp() throws Exception {
        super();
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure("configuration/log4j.properties");
        org.apache.log4j.helpers.LogLog.setQuietMode(true);
        try {
            FortumoApp lsn = new FortumoApp();
            lsn.initSystem();
            lsn.run();
        } catch (Throwable var4) {
            log.error("FATAL error occured, system is interrupted", var4);
            System.exit(-1);
        }

    }


    @Override
    public void initSystem() throws Exception {
        super.initSystem();
        this.getThreadListers().clear();
        this.getThreadListers().add(new FileThreadLister2("configuration/thread/"));
    }
}
