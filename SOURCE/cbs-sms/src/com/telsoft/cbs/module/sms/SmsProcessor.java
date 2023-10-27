package com.telsoft.cbs.module.sms;

import com.telsoft.thread.ManageableThread;
import com.telsoft.thread.ParameterType;
import com.telsoft.util.AppException;

import java.util.List;
import java.util.Vector;

/**
 *
 *  Package==com.example.com
 * 	Handler==SourceClassName
 * 		Handler==Class
 * 	      Condition
 * 		    Source==Mobifone
 * 		  Handler==CodeClassName
 * 			  Handler==ClassName
 * 			      Condition
 * 				      Codes==Code1, Code2, Code3
 * 				  Handler==ClassName
 * 					  Codes==Code1
 * 				  Handler==ClassName
 * 					  Codes==Code2
 * 				  Handler==ClassName
 * 					  Codes==Code3
 * 			  Handler==ClassName
 * 				  Codes==Code4, Code5, Code6
 * 				  Handler==ClassName
 * 					  Codes==Code4, Code5
 * 				  Handler==ClassName
 * 					  Codes==Code2
 * 				  Handler==ClassName
 * 					  Codes==Code3
 * 			  Handler==ClassName
 * 				  Codes==Code7
 * 		Handler==Class
 * 		  Source==Other
 *
 */
public abstract class SmsProcessor extends ManageableThread {
    private int reloadInterval;

    @Override
    public void fillParameter() throws AppException {
        super.fillParameter();
        reloadInterval = loadInteger("reload-interval");
    }

    @Override
    public Vector getParameterDefinition() {
        Vector vtReturn = new Vector();
        vtReturn.add(createParameter("reload-interval", "", ParameterType.PARAM_TEXTBOX_MASK, "00000", ""));
        vtReturn.addAll(super.getParameterDefinition());
        return vtReturn;
    }


    @Override
    protected void processSession() throws Exception {
        while (isRunning()) {
            processMessages();
            Thread.sleep(reloadInterval);
        }
    }

    protected abstract void processMessages();

    protected abstract void processMessage(SmsMessage message);
}
