package com.telsoft.cbs.module.fortumo.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import org.apache.log4j.Logger;

import java.util.Vector;

public class HazelcastServerStarter extends ManageableThread {
    private static Logger logger = Logger.getLogger("HAZELCAST-SERVER");

    public static HazelcastInstance hazelcast;

    private Config serverConfig;
    private String strHazelcastClusterName;
    private String strHazelcastMapName;
    private String strHazelcastConfigFile;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.addElement(createParameter("Hazelcast Config File", "", ParameterType.PARAM_TEXTBOX_MAX, "500"));
        vtReturn.addElement(createParameter("Hazelcast Cluster Name", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));
        vtReturn.addElement(createParameter("Hazelcast MapName", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        strHazelcastConfigFile = loadString("Hazelcast Config File");
        strHazelcastClusterName = loadString("Hazelcast Cluster Name");
        strHazelcastMapName = loadString("Hazelcast MapName");
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();

        //FileSystemXmlConfig fileSystemXmlConfig = new FileSystemXmlConfig(strHazelcastConfigFile);
        serverConfig = new XmlConfigBuilder(strHazelcastConfigFile).build();
        serverConfig.setClusterName(strHazelcastClusterName);
        serverConfig.setProperty("hazelcast.logging.details.enabled", "false");
        serverConfig.setProperty("hazelcast.logging.type", "none");
        hazelcast = Hazelcast.newHazelcastInstance(serverConfig);
        hazelcast.getMap(strHazelcastMapName);
    }

    @Override
    protected void processSession() throws Exception {
        this.getThreadID();
        while (miThreadCommand != ThreadConstant.THREAD_STOP && miThreadStatus != ThreadConstant.THREAD_STOPPED) {
            if (!checkAlive()) {
                logger.error("Hazelcast Server is down...");
                break;
            }
            logMonitor("Hazelcast Server still alive...");
            logMonitor("Map name: " + hazelcast.getMap(strHazelcastMapName).getName() + "   Cache size: " + hazelcast.getMap(strHazelcastMapName).size());
            logger.info("Hazelcast still alive...");
            fillLogFile();
            validateParameter();
            Thread.sleep(this.miDelayTime * 1000);
        }
    }

    @Override
    protected void afterSession() throws Exception {
        super.afterSession();
        logMonitor("Hazelcast Server is shutting down...");
        if (hazelcast != null) {
            hazelcast.shutdown();
            hazelcast = null;
            logger.info("Hazelcast Server is down...");
        }
    }

    public boolean checkAlive() {
        try {
            hazelcast.getMap(strHazelcastMapName).get("");
            return true;
        } catch (Exception ex) {
            logMonitor("ERROR when checkAlive HazelCast Server: " + strHazelcastMapName);
            return false;
        }
    }
}
