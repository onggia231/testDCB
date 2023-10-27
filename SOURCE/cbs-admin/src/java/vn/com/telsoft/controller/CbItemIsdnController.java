/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import vn.com.telsoft.entity.CBItemIsdn;
import vn.com.telsoft.entity.CBList;
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.model.CBItemIsdnModel;
import vn.com.telsoft.model.CbListModel;
import vn.com.telsoft.model.CbStoreModel;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author HaNV
 */
@Named
@ViewScoped
public class CbItemIsdnController extends TSFuncTemplate implements Serializable {

    private static final long serialVersionUID = 3056494023762359101L;

    private List<CBList> mList;
    private CBList mtmpCbList;
    private List<CBList> mFilter;
    private CbListModel cbListModel;
    private CBItemIsdn cbItemIsdn;
    private List<CBStore> cbStoreList;
    private CBStore cbStore;
    private CbStoreModel cbStoreModel;
    private CBItemIsdnModel cbItemIsdnModel;
    private boolean found;
    private Long listId;
    private String isdn;
    private boolean searchClick;

    public boolean isSearchClick() {
        return searchClick;
    }

    public void setSearchClick(boolean searchClick) {
        this.searchClick = searchClick;
    }

    public CbItemIsdnController() throws Exception {


        mtmpCbList = new CBList();
        mtmpCbList.setStatus(1);
        mtmpCbList.setListType(0);
        cbListModel = new CbListModel();
        cbItemIsdnModel = new CBItemIsdnModel();
        mList = cbListModel.getCbList(mtmpCbList);
        try {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String parameter = params.get("cbListId");
            listId = Long.parseLong(parameter);
        } catch (Exception e) {
            listId = null;
        }
        cbItemIsdn = new CBItemIsdn();
        isdn = "";
        cbStore = new CBStore();
        cbStore.setStatus(1L);
        cbStoreModel = new CbStoreModel();
        cbStoreList = cbStoreModel.getCBStoreAll(cbStore);
        this.isVIEW = false;
        this.isADD = false;
        found = true;
        searchClick = false;
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void search() throws Exception {
        try {
            searchClick = true;
            cbItemIsdn.setListId(listId);
            cbItemIsdn.setIsdn(isdn);
            cbItemIsdn = cbItemIsdnModel.getCbItemIsdn(this.cbItemIsdn).get(0);
//            listId = cbItemIsdn.getListId();
//            isdn = cbItemIsdn.getIsdn();
            this.isVIEW = true;
            found = true;
        } catch (Exception e) {
            this.isVIEW = false;
            cbItemIsdn = new CBItemIsdn();
            cbItemIsdn.setIsdn(isdn);
            cbItemIsdn.setListId(listId);
            found = false;
        }
    }

    public boolean isFound() {
        return found;
    }

    public void setFound(boolean found) {
        this.found = found;
    }

    public void delete() throws Exception {
        this.isVIEW = false;
    }

    @Override
    public void changeStateAdd() throws Exception {
        super.changeStateAdd();
        found = true;
        isdn = "";
        listId = null;
        cbItemIsdn = new CBItemIsdn();
    }

    public boolean getIsView() throws Exception {
        return super.isVIEW;
    }

    public boolean isView() throws Exception {
        return super.isVIEW;
    }

    @Override
    public void handleOK() throws Exception {
        if (!getPermission("I")) {
            return;
        }
        cbItemIsdn.setIsdn(isdn);
        cbItemIsdn.setListId(listId);
        if (cbItemIsdnModel.getCbItemIsdn(cbItemIsdn).size() > 0) {
            ClientMessage.logPErrAdd("ERR-10012");
            return;
        }
        cbItemIsdnModel.insert(cbItemIsdn);
        search();
//        cbItemIsdn = new CBItemIsdn();
        ClientMessage.logAdd();
        found = true;
        handleCancel();

    }
    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleDelete() throws Exception {
        if (!getPermission("D")) {
            return;
        }
        cbItemIsdnModel.delete(cbItemIsdn);
        cbItemIsdn = new CBItemIsdn();
        isdn = "";
        listId = null;
        ClientMessage.logDelete();
        this.isVIEW = false;
        searchClick = false;
    }
    //////////////////////////////////////////////////////////////////////////////////


    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public boolean getIsDisplayBtnConfirm() throws Exception {
        return this.isADD;
    }

    public void handleCancel() throws Exception {
        this.isADD = false;
    }

    public List<CBList> getmList() {
        return mList;
    }

    public void setmList(List<CBList> mList) {
        this.mList = mList;
    }

    public CBList getMtmpCbList() {
        return mtmpCbList;
    }

    public void setMtmpCbList(CBList mtmpCbList) {
        this.mtmpCbList = mtmpCbList;
    }

    public CbListModel getCbListModel() {
        return cbListModel;

    }

    public void setCbListModel(CbListModel cbListModel) {
        this.cbListModel = cbListModel;
    }

    public List<CBList> getmFilter() {
        return mFilter;
    }

    public void setmFilter(List<CBList> mFilter) {
        this.mFilter = mFilter;
    }

    public CBItemIsdn getCbItemIsdn() {
        return cbItemIsdn;
    }

    public void setCbItemIsdn(CBItemIsdn cbItemIsdn) {
        this.cbItemIsdn = cbItemIsdn;
    }

    public List<CBStore> getCbStoreList() {
        return cbStoreList;
    }

    public void setCbStoreList(List<CBStore> cbStoreList) {
        this.cbStoreList = cbStoreList;
    }

    public CBStore getCbStore() {
        return cbStore;
    }

    public void setCbStore(CBStore cbStore) {
        this.cbStore = cbStore;
    }

}
