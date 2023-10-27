package com.telsoft.cbs.module.fortumo.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ParameterUtil;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;
import lombok.Getter;
import lombok.Setter;
import org.apache.log4j.Logger;

import java.util.Vector;

@Getter
@Setter
public class HazelcastClientStarter extends ManageableThread {
    private static Logger logger = Logger.getLogger("HAZELCAST-CLIENT");

    public static HazelcastInstance hazelcastClient;
    public static HzClient hzClient = new HzClient();

    private ClientConfig clientConfig;
    private String strHazelcastClusterName;
    private String strHazelcastMapName;
    private String strHazelcastConfigFile;
    private int connectionTimeout;
    private int ttl;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.addElement(createParameter("Hazelcast Config File", "", ParameterType.PARAM_TEXTBOX_MAX, "500"));
        vtReturn.addElement(createParameter("Hazelcast Cluster Name", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));
        vtReturn.addElement(createParameter("Hazelcast MapName", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));
        vtReturn.add(ParameterUtil.createParameter("Time-to-Live", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));
        vtReturn.add(ParameterUtil.createParameter("Connection Timeout", "", ParameterType.PARAM_TEXTBOX_MAX, "256", ""));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        strHazelcastConfigFile = loadString("Hazelcast Config File");
        strHazelcastClusterName = loadString("Hazelcast Cluster Name");
        strHazelcastMapName = loadString("Hazelcast MapName");
        ttl = loadInteger("Time-to-Live");
        connectionTimeout = loadInteger("Connection Timeout");
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();
        clientConfig = new XmlClientConfigBuilder(strHazelcastConfigFile).build();
        clientConfig.setClusterName(strHazelcastClusterName);
        clientConfig.getNetworkConfig().setConnectionTimeout(connectionTimeout);
        clientConfig.setProperty("hazelcast.logging.details.enabled", "false");
        clientConfig.setProperty("hazelcast.logging.type", "none");
        hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
        hzClient.setHazelcastInstance(hazelcastClient);
        hzClient.setCluster(strHazelcastClusterName);
        hzClient.setMapName(strHazelcastMapName);
        hzClient.setTtl(ttl);
        hzClient.setMap(hazelcastClient.getMap(strHazelcastMapName));
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
            logMonitor("Map name: " + hazelcastClient.getMap(strHazelcastMapName).getName() + "   Cache size: " + hazelcastClient.getMap(strHazelcastMapName).size());
            logger.info("Hazelcast still alive...");
            fillLogFile();
            validateParameter();
            Thread.sleep(this.miDelayTime * 1000);
        }
    }

    @Override
    protected void afterSession() throws Exception {
        super.afterSession();
        logMonitor("Hazelcast Client is shutting down...");
        if (hazelcastClient != null) {
            hazelcastClient.shutdown();
            hazelcastClient = null;
            logger.info("Hazelcast Client is down...");
        }
    }

    public boolean checkAlive() {
        try {
            hazelcastClient.getMap(strHazelcastMapName).get("");
            return true;
        } catch (Exception ex) {
            logMonitor("ERROR when checkAlive HazelCast Client: " + strHazelcastMapName);
            return false;
        }
    }
}
