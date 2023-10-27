package com.telsoft.cbs.module.fortumo.hazelcast;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.XmlClientConfigBuilder;
import com.hazelcast.core.HazelcastInstance;
import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.thread.ThreadConstant;
import com.telsoft.util.AppException;

import java.util.Vector;

public class MemberCheckAlive extends ManageableThread {
    public static HazelcastInstance hazelcast;

    private String strHazelcastConfigFile;
    private String strHazelcastClusterName;
    private String strHazelcastMapName;

    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.addElement(createParameter("HazelcastConfigFile", "", ParameterType.PARAM_TEXTBOX_MAX, "500"));
        vtReturn.addElement(createParameter("HazelcastClusterName", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));
        vtReturn.addElement(createParameter("HazelcastMapName", "", ParameterType.PARAM_TEXTBOX_MAX, "100"));

        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        strHazelcastConfigFile = loadString("HazelcastConfigFile");
        strHazelcastClusterName = loadString("HazelcastClusterName");
        strHazelcastMapName = loadString("HazelcastMapName");
    }

    @Override
    protected void beforeSession() throws Exception {
        super.beforeSession();

        try {
            ClientConfig config = new XmlClientConfigBuilder(strHazelcastConfigFile).build();
            config.setClusterName(strHazelcastClusterName);
            hazelcast = HazelcastClient.newHazelcastClient(config);
        } catch (Exception ex) {
            logMonitor("Error loading config file or Hazelcast Server is down...");
            logMonitor("Restart Hazelcast Server or Hazelcast Client please...");
        }

    }

    @Override
    protected void afterSession() throws Exception {
        super.afterSession();
        if (hazelcast != null) {
            hazelcast.shutdown();
            hazelcast = null;
        }
    }

    @Override
    protected void processSession() throws Exception {
        this.getThreadID();
        while (miThreadCommand != ThreadConstant.THREAD_STOP && miThreadStatus != ThreadConstant.THREAD_STOPPED) {
            if (!checkAlive()) {
                break;
            }
            logMonitor("Map name: " + hazelcast.getMap(strHazelcastMapName).getName() + "   Cache size: " + hazelcast.getMap(strHazelcastMapName).size());
            fillLogFile();
            validateParameter();
            Thread.sleep(this.miDelayTime * 1000);

        }
    }

    public boolean checkAlive() {
        try {
            hazelcast.getMap(strHazelcastMapName).get("");
            return true;
        } catch (Exception ex) {
            logMonitor("ERROR when checkAlive HazelCast: " + strHazelcastMapName);
            return false;
        }
    }
}