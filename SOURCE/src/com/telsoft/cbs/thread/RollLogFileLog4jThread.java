package com.telsoft.cbs.thread;

import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;
import com.telsoft.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Vector;

public class RollLogFileLog4jThread extends ManageableThread {

    private String loggerName;
    private String logContent;

    @Override
    public Vector getParameterDefinition() {

        Vector vtReturn = new Vector();
        vtReturn.addAll(super.getParameterDefinition());
        vtReturn.add(createParameter("LoggerName", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        vtReturn.add(createParameter("LogContent", "", ParameterType.PARAM_TEXTBOX_MAX, "99999", ""));
        return vtReturn;
    }

    public void fillParameter() throws AppException {
        super.fillParameter();
        loggerName = loadString("LoggerName");
//        logContent = StringUtil.nvl(this.getParameter("LogContent"), "");
        logContent = (String) this.getParameter("LogContent");

    }


    @Override
    protected void processSession() throws Exception {
        Logger logger = LoggerFactory.getLogger(loggerName);
        logger.info(logContent);
        logMonitor(loggerName + " is rolled");
    }
}
