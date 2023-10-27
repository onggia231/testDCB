package com.telsoft.cbs;

import com.telsoft.thread.FileThreadLister2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import telsoft.gateway.core.GatewayConfiguration;
import telsoft.gateway.core.GatewayManager;

import java.io.File;

public class CBApp extends GatewayManager {
    private static final Logger log = LoggerFactory.getLogger(CBApp.class);

    public CBApp(GatewayConfiguration config) throws Exception {
        super(config);
    }

    public static void main(String[] args) {
        if (args != null && args.length > 0) {
            SERVER_CONFIG_FILE = args[0];
            System.out.println("Using config file : " + SERVER_CONFIG_FILE);
        }

        File file = new File(SERVER_CONFIG_FILE);
        if (!file.exists()) {
            System.out.println("Config file : " + SERVER_CONFIG_FILE + " doesn't existed. Application will be halt.");
            System.exit(1);
        }

        if (!file.canRead()) {
            System.out.println("Config file : " + SERVER_CONFIG_FILE + " cannot read or access. Application will be halt.");
            System.exit(2);
        }

        try {
            GatewayConfiguration config = new GatewayConfiguration();
            CBApp lsn = new CBApp(config);
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
