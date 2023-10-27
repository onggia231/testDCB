package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.util.ResourceBundleUtil;
import org.apache.commons.lang3.SerializationUtils;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import vn.com.telsoft.entity.CBLimitProfile;
import vn.com.telsoft.model.CBLimitProfileModel;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@ViewScoped
@Named
public class CbLimitProfileController extends TSFuncTemplate implements Serializable {

    private static final long serialVersionUID = -2455613327962942336L;

    private CBLimitProfileModel model;
    private CBLimitProfile limitProfile;
    private CBLimitProfile limitProfileSelected;
    private List<CBLimitProfile> listLimitProfile;
    private List<CBLimitProfile> listLimitProfileFiltered;

    private int idxEditFilter;

    public CbLimitProfileController() throws Exception {
        model = new CBLimitProfileModel();
        listLimitProfile = model.getData();
        if (listLimitProfile != null && listLimitProfile.size() > 0) {
            limitProfileSelected = listLimitProfile.get(0);
            limitProfile = limitProfileSelected;
        }
    }

    @Override
    public void changeStateAdd() throws Exception {
        super.changeStateAdd();
        limitProfile = new CBLimitProfile();
    }

    public void changeStateEdit(CBLimitProfile ett) throws Exception {
        super.changeStateEdit();
        selectedIndex = listLimitProfile.indexOf(ett);
        if (listLimitProfileFiltered != null && listLimitProfileFiltered.size() > 0) {
            idxEditFilter = listLimitProfileFiltered.indexOf(ett);
        }
        limitProfile = (CBLimitProfile) SerializationUtils.clone(ett);
    }

    public void changeStateCopy(CBLimitProfile ett) throws Exception {
        super.changeStateCopy();
        limitProfile = ett;
    }

    public void onRowSelect(SelectEvent evt) throws Exception {
        this.limitProfile = (CBLimitProfile) evt.getObject();
        limitProfileSelected = limitProfile;
    }

    public void onRowUnselect() throws Exception {
        limitProfile = new CBLimitProfile();
        limitProfileSelected = null;
    }

    @Override
    public void handleOK() throws Exception {
        if (isADD || isCOPY) {
            //Check permission
            if (!getPermission("I")) {
                return;
            }
            model.insert(limitProfile);
            listLimitProfile.add(0, limitProfile);
            PrimeFaces.current().executeScript("PF('table_limit_profile').clearFilters();");
            ClientMessage.logAdd();
            //Message to client
        } else if (isEDIT) {
            //Check permission
            if (!getPermission("U")) {
                return;
            }
            model.update(limitProfile);
            listLimitProfile.set(selectedIndex, limitProfile);
            if (listLimitProfileFiltered != null && listLimitProfileFiltered.size() > 0) {
                listLimitProfileFiltered.set(idxEditFilter, limitProfile);
            }
            ClientMessage.logUpdate();
            //Message to client
        }
        handleCancel();
    }

    @Override
    public void handleDelete() throws Exception {
        handleDelete(null);
    }

    public void handleDelete(CBLimitProfile ett) throws Exception {
        if (!getPermission("D")) {
            return;
        }
        try {
            model.delete(ett);
        } catch (Exception ex) {
            if (ex.getMessage().contains("ERROR_EXIST_LIMIT_PROFILE")) {
                ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_CB_LIMIT_PROFILE", "ERROR_EXIST_LIMIT_PROFILE"));
                return;
            }
        }
        listLimitProfile = model.getData();
        limitProfileSelected = null;
        limitProfile = new CBLimitProfile();
        ClientMessage.logDelete();
        handleCancel();
    }

    public void handleCancel() throws Exception {
        super.handleCancel();
        //Reset form
        if (limitProfileSelected != null) {
            limitProfileSelected = (CBLimitProfile) SerializationUtils.clone(limitProfile);
        } else {
            limitProfile = new CBLimitProfile();
        }
    }

    public boolean getIsDisplayBtnConfirm() throws Exception {
        return this.isADD || this.isEDIT || this.isCOPY;
    }

    public CBLimitProfile getLimitProfile() {
        return limitProfile;
    }

    public void setLimitProfile(CBLimitProfile limitProfile) {
        this.limitProfile = limitProfile;
    }

    public CBLimitProfile getLimitProfileSelected() {
        return limitProfileSelected;
    }

    public void setLimitProfileSelected(CBLimitProfile limitProfileSelected) {
        this.limitProfileSelected = limitProfileSelected;
    }

    public List<CBLimitProfile> getListLimitProfile() {
        return listLimitProfile;
    }

    public void setListLimitProfile(List<CBLimitProfile> listLimitProfile) {
        this.listLimitProfile = listLimitProfile;
    }

    public List<CBLimitProfile> getListLimitProfileFiltered() {
        return listLimitProfileFiltered;
    }

    public void setListLimitProfileFiltered(List<CBLimitProfile> listLimitProfileFiltered) {
        this.listLimitProfileFiltered = listLimitProfileFiltered;
    }

}
