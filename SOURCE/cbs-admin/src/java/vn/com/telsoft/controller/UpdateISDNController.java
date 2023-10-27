/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.util.ResourceBundleUtil;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import vn.com.telsoft.entity.CBLimitProfile;
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.entity.CBSubStore;
import vn.com.telsoft.entity.CBSubscriber;
import vn.com.telsoft.model.CBLimitProfileModel;
import vn.com.telsoft.model.SearchISDNModel;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author TUNGLM, TELSOFT
 */
@Named
@ViewScoped
public class UpdateISDNController extends TSFuncTemplate implements Serializable {

    private static final long serialVersionUID = -7098807366842265994L;

    @Inject
    private SearchISDNController searchISDNController;
    @Inject
    private InfoISDNController infoISDNController;
    @Inject
    private InfoISDNTransController infoISDNTransController;


    public UpdateISDNController() throws Exception {
    }

    @Override
    public void handleOK() throws Exception {

    }

    @Override
    public void handleDelete() throws Exception {

    }

    public void handleSearchListISDN() throws Exception {
        this.searchISDNController.handleSearch();
        setListSubscriber(getListSubscriber());
        setSubscriberSelected(new CBSubscriber());
        if (getListSubscriber() != null && getListSubscriber().size() > 0) {
            setbDISPLAYSearchISDN(true);
            setbDISPLAYInfoISDN(false);
            setbDISPLAYInfoISDNTrans(false);
        } else {
            setbDISPLAYSearchISDN(false);
            setbDISPLAYInfoISDN(false);
            setbDISPLAYInfoISDNTrans(false);
            setlStoreId(null);
            setMtmpSubStore(null);
            ClientMessage.logPErr("subNotFound");
            return;
        }
    }

    public String convertDateTime(Date date) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(date);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void handleViewISDN(SelectEvent evt) throws Exception {
        CBSubscriber cbSubscriber = (CBSubscriber) evt.getObject();
        this.searchISDNController.handleView(cbSubscriber);
        setMtmpSubscriber(getMtmpSubscriberSearch());
        setbDISPLAYInfoISDN(true);
        setbDISPLAYInfoISDNTrans(true);
        setMlistStore(getMtmpSubscriberSearch().getLstStore());
        List<CBStore> lstCBStore = getMlistStore();
        if (lstCBStore != null && lstCBStore.size() > 0) {
            setlStoreId(lstCBStore.get(0).getId());
            setMtmpSubStore(getMtmpSubscriber().getMapSubStore().get(getlStoreId()));
        }
    }

    public void handleUnViewISDN(UnselectEvent evt) throws Exception {
        setbDISPLAYInfoISDN(false);
        setbDISPLAYInfoISDNTrans(false);
        setlStoreId(null);
        setMtmpSubStore(null);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void handleSaveInfoISDN() throws Exception {
        this.infoISDNController.handleSave();
        this.searchISDNController.handleSearch();
        setListSubscriber(getListSubscriber());
        ClientMessage.logUpdate();
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void handleSaveInfoISDNTrans() throws Exception {
        this.infoISDNTransController.handleSave();
        ClientMessage.logUpdate();
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void onStoreChange() throws Exception {
        setMtmpSubStore(getMtmpSubscriber().getMapSubStore().get(getlStoreId()));
    }

    //////////////////////////////////////////////////////////////////////////////////

    //Getters, Setters
    public SearchISDNController getSearchISDNController() {
        return searchISDNController;
    }

    public void setSearchISDNController(SearchISDNController searchISDNController) {
        this.searchISDNController = searchISDNController;
    }

    public InfoISDNController getInfoISDNController() {
        return infoISDNController;
    }

    public void setInfoISDNController(InfoISDNController infoISDNController) {
        this.infoISDNController = infoISDNController;
    }

    public InfoISDNTransController getInfoISDNTransController() {
        return infoISDNTransController;
    }

    public void setInfoISDNTransController(InfoISDNTransController infoISDNTransController) {
        this.infoISDNTransController = infoISDNTransController;
    }

    public String getIsdn() {
        return this.searchISDNController.getIsdn();
    }

    public void setIsdn(String isdn) {
        this.searchISDNController.setIsdn(isdn);
    }

    public CBSubscriber getMtmpSubscriberSearch() {
        return this.searchISDNController.getMtmpSubscriber();
    }

    public void setMtmpSubscriberSearch(CBSubscriber mtmpSubscriber) {
        this.searchISDNController.setMtmpSubscriber(mtmpSubscriber);
    }

    public CBSubscriber getMtmpSubscriber() {
        return this.infoISDNController.getMtmpSubscriber();
    }

    public void setMtmpSubscriber(CBSubscriber mtmpSubscriber) {
        this.infoISDNController.setMtmpSubscriber(mtmpSubscriber);
    }

    public boolean isbDISPLAYSearchISDN() {
        return this.searchISDNController.isbDISPLAY();
    }

    public void setbDISPLAYSearchISDN(boolean bDISPLAY) {
        this.searchISDNController.setbDISPLAY(bDISPLAY);
    }

    public boolean isbDISPLAYInfoISDN() {
        return this.infoISDNController.isbDISPLAY();
    }

    public void setbDISPLAYInfoISDN(boolean bDISPLAY) {
        this.infoISDNController.setbDISPLAY(bDISPLAY);
    }

    public CBSubStore getMtmpSubStore() {
        return this.infoISDNTransController.getMtmpSubStore();
    }

    public void setMtmpSubStore(CBSubStore mtmpSubStore) {
        this.infoISDNTransController.setMtmpSubStore(mtmpSubStore);
    }

    public List<CBStore> getMlistStore() {
        return this.infoISDNTransController.getMlistStore();
    }

    public void setMlistStore(List<CBStore> mlistStore) {
        this.infoISDNTransController.setMlistStore(mlistStore);
    }

    public Long getlStoreId() {
        return this.infoISDNTransController.getlStoreId();
    }

    public void setlStoreId(Long lStoreId) {
        this.infoISDNTransController.setlStoreId(lStoreId);
    }

    public boolean isbDISPLAYInfoISDNTrans() {
        return this.infoISDNTransController.isbDISPLAY();
    }

    public List<CBLimitProfile> getListLimitProfile() {
        return this.infoISDNTransController.getListLimitProfile();
    }

    public void setListLimitProfile(List<CBLimitProfile> listLimitProfile) {
        this.infoISDNTransController.setListLimitProfile(listLimitProfile);
    }

    public void setbDISPLAYInfoISDNTrans(boolean bDISPLAY) {
        this.infoISDNTransController.setbDISPLAY(bDISPLAY);
    }

    public List<CBSubscriber> getListSubscriber() {
        return this.searchISDNController.getListSubScriber();
    }

    public void setListSubscriber(List<CBSubscriber> listSubscriber) {
        this.searchISDNController.setListSubScriber(listSubscriber);
    }

    public CBSubscriber getSubscriberSelected() {
        return this.searchISDNController.getSubscriberSelected();
    }

    public void setSubscriberSelected(CBSubscriber subscriberSelected) {
        this.searchISDNController.setSubscriberSelected(subscriberSelected);
    }
}
