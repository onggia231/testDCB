/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import vn.com.telsoft.entity.CBLimitProfile;
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.entity.CBSubStore;
import vn.com.telsoft.model.InfoISDNTransModel;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author TUNGLM, TELSOFT
 */
@Named
@ViewScoped
public class InfoISDNTransController implements Serializable {

    private static final long serialVersionUID = 4780060029466034361L;

    private InfoISDNTransModel mmodel;
    private CBSubStore mtmpSubStore;
    private List<CBStore> mlistStore;
    private Long lStoreId;
    private boolean bDISPLAY = false;
    private List<CBLimitProfile> listLimitProfile;

    public InfoISDNTransController() throws Exception {
        mmodel = new InfoISDNTransModel();
        listLimitProfile = new ArrayList<>();
        listLimitProfile = mmodel.getListLimitProfile();
//        mlistStore = mmodel.getListAllStore();
    }
    //////////////////////////////////////////////////////////////////////////////////


    public void handleSave() throws Exception {
        mmodel.updateInfoISDNTrans(mtmpSubStore);
    }
    //////////////////////////////////////////////////////////////////////////////////

    //Getters, Setters
    public CBSubStore getMtmpSubStore() {
        return mtmpSubStore;
    }

    public void setMtmpSubStore(CBSubStore mtmpSubStore) {
        this.mtmpSubStore = mtmpSubStore;
    }

    public List<CBStore> getMlistStore() {
        return mlistStore;
    }

    public void setMlistStore(List<CBStore> mlistStore) {
        this.mlistStore = mlistStore;
    }

    public Long getlStoreId() {
        return lStoreId;
    }

    public void setlStoreId(Long lStoreId) {
        this.lStoreId = lStoreId;
    }

    public boolean isbDISPLAY() {
        return bDISPLAY;
    }

    public void setbDISPLAY(boolean bDISPLAY) {
        this.bDISPLAY = bDISPLAY;
    }

    public List<CBLimitProfile> getListLimitProfile() {
        return listLimitProfile;
    }

    public void setListLimitProfile(List<CBLimitProfile> listLimitProfile) {
        this.listLimitProfile = listLimitProfile;
    }
}
