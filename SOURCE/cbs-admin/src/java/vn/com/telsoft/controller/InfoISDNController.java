/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import vn.com.telsoft.entity.CBSubscriber;
import vn.com.telsoft.model.InfoISDNModel;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;

/**
 * @author TUNGLM, TELSOFT
 */
@Named
@ViewScoped
public class InfoISDNController implements Serializable {

    private static final long serialVersionUID = -3752263906899811271L;

    private InfoISDNModel mmodel;
    private CBSubscriber mtmpSubscriber;
    private boolean bDISPLAY = false;

    public InfoISDNController() throws Exception {
        mmodel = new InfoISDNModel();
    }
    //////////////////////////////////////////////////////////////////////////////////


    public void handleSave() throws Exception {
        mmodel.updateInfoISDN(mtmpSubscriber);
    }
    //////////////////////////////////////////////////////////////////////////////////

    //Getters, Setters
    public CBSubscriber getMtmpSubscriber() {
        return mtmpSubscriber;
    }

    public void setMtmpSubscriber(CBSubscriber mtmpSubscriber) {
        this.mtmpSubscriber = mtmpSubscriber;
    }

    public boolean isbDISPLAY() {
        return bDISPLAY;
    }

    public void setbDISPLAY(boolean bDISPLAY) {
        this.bDISPLAY = bDISPLAY;
    }
}
