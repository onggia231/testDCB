/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.admin.gui.data.AppGuiModel;
import com.faplib.lib.admin.gui.entity.AppGUI;
import org.apache.commons.lang3.SerializationUtils;
import vn.com.telsoft.model.AppModel;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author CHIENDX, TELSOFT
 */
@Named
@ViewScoped
public class AppsController extends TSFuncTemplate implements Serializable {

    private List<AppGUI> mlistApp;
    private AppGUI mtmpApp;
    private List<AppGUI> mselectedApp;
    private AppModel mmodel;

    public AppsController() throws Exception {
        mmodel = new AppModel();
        mlistApp = mmodel.getListApp();
    }
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void changeStateAdd() throws Exception {
        super.changeStateAdd();
        mtmpApp = new AppGUI();
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void changeStateEdit(AppGUI app) throws Exception {
        super.changeStateEdit();
        selectedIndex = mlistApp.indexOf(app);
        mtmpApp = (AppGUI) SerializationUtils.clone(app);
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void changeStateCopy(AppGUI app) throws Exception {
        super.changeStateCopy();
        mtmpApp = app;
    }
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleOK() throws Exception {
        if (isADD || isCOPY) {
            //Check permission
            if (!getPermission("I")) {
                return;
            }

            mmodel.add(mtmpApp);
            mlistApp.add(0, mtmpApp);

            //Reset form
            mtmpApp = new AppGUI();

            //Message to client
            ClientMessage.logAdd();

        } else if (isEDIT) {
            //Check permission
            if (!getPermission("U")) {
                return;
            }

            mmodel.edit(mtmpApp);
            mlistApp.set(selectedIndex, mtmpApp);

            //Message to client
            ClientMessage.logUpdate();
        }
    }
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleDelete() throws Exception {
        handleDelete(null);
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void handleDelete(AppGUI ett) throws Exception {
        //Check permission
        if (!getPermission("D")) {
            return;
        }

        if (ett == null) {
            mmodel.delete(mselectedApp);

        } else {
            mmodel.delete(Collections.singletonList(ett));
        }


        mlistApp = mmodel.getListAll();
        mselectedApp = null;

        //Message to client
        ClientMessage.logDelete();
    }
    //////////////////////////////////////////////////////////////////////////////////

    public boolean isIsSelectedApp() {
        return mselectedApp != null && !mselectedApp.isEmpty();
    }

    //Getters
    public List<AppGUI> getMlistApp() {
        return mlistApp;
    }

    public AppGUI getMtmpApp() {
        return mtmpApp;
    }

    //Setters
    public void setMtmpApp(AppGUI mtmpApp) {
        this.mtmpApp = mtmpApp;
    }

    public List<AppGUI> getMselectedApp() {
        return mselectedApp;
    }

    public void setMselectedApp(List<AppGUI> mselectedApp) {
        this.mselectedApp = mselectedApp;
    }
}
