/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.util.ResourceBundleUtil;
import org.apache.commons.lang.SerializationUtils;
import org.primefaces.PrimeFaces;
import vn.com.telsoft.entity.SMSCommand;
import vn.com.telsoft.model.SMSCommandModel;

import javax.faces.model.SelectItem;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author
 */
@Named
@ViewScoped
public class SmsCommandController extends TSFuncTemplate implements Serializable {
    private static final long serialVersionUID = -1487321853420185603L;

    private List<SMSCommand> mlistSMS;
    private List<SMSCommand> mlistFilteredSMS;
    private SMSCommand mtmpSMS;
    private SMSCommand mtmpSMSInput;
    private List<SMSCommand> mselectedSMS;
    private SMSCommand selectedSMS;
    private SMSCommandModel mmodel = new SMSCommandModel();
    private SelectItem[] mstatusOption;

    public SmsCommandController() throws Exception {
        this.mlistSMS = this.mmodel.getAll();
        //
//        mselectedSMS = new ArrayList<>();
//        mselectedSMS.add(mlistSMS.get(0));
//        //
//        mtmpSMS = new SMSCommand(mlistSMS.get(0));
        //
        mstatusOption = new SelectItem[3];
        mstatusOption[0] = new SelectItem("", "");
        mstatusOption[1] = new SelectItem("1", ResourceBundleUtil.getAMObjectAsString("PP_COMMONS", "enable"));
        mstatusOption[2] = new SelectItem("0", ResourceBundleUtil.getAMObjectAsString("PP_COMMONS", "disable"));

    }
    public void onRowSelected() {
        mtmpSMS = new SMSCommand(selectedSMS);
    }
    public void onRowUnSelected() {
        mtmpSMS = new SMSCommand();
    }

    public void changeStateAdd() throws Exception {
        super.changeStateAdd();
        this.mtmpSMS = new SMSCommand();
    }

    private int indexFilter;

    public void changeStateEdit(SMSCommand app) throws Exception {
        super.changeStateEdit();
        this.selectedIndex = this.mlistSMS.indexOf(app);
        this.mtmpSMS = (SMSCommand) SerializationUtils.clone(app);
        ////////////////////////////////
        if (null != mlistFilteredSMS) {
            for (int i = 0; i < mlistFilteredSMS.size(); i++) {
                if (mlistFilteredSMS.get(i).getCmdCode().equals(app.getCmdCode())) {
                    indexFilter = i;
                }
            }
        }
    }

    public void changeStateCopy(SMSCommand app) throws Exception {
        super.changeStateCopy();
        this.mtmpSMS = (SMSCommand) SerializationUtils.clone(app);
    }

    public void handleOK() throws Exception {

        if (!this.isADD && !this.isCOPY) {
            if (this.isEDIT) {
                mtmpSMSInput = new SMSCommand(mtmpSMS);
                if (!vlaDataEdit(mtmpSMSInput,mlistSMS)) {
                    return;
                }
                if (!this.getPermission("U")) {
                    return;
                }

                this.mmodel.edit(this.mtmpSMS);
                this.mlistSMS.set(this.selectedIndex, this.mtmpSMS);
                if (null != mlistFilteredSMS) {
                    mlistFilteredSMS.set(indexFilter, this.mtmpSMS);
                }
                ClientMessage.logUpdate();
            }
        } else {
            if (!this.getPermission("I")) {
                return;
            }
            mtmpSMSInput = new SMSCommand(mtmpSMS);
            if (!vlaData(mtmpSMSInput)) {
                return;
            }
            this.mmodel.add(mtmpSMSInput);
            this.mlistSMS.add(0, mtmpSMSInput);
            PrimeFaces.current().executeScript("PF('table_sms').clearFilters();");
            this.mtmpSMS = new SMSCommand();
            ClientMessage.logAdd();
        }
        handleCancel();

    }

    public void handleDelete() throws Exception {
        this.handleDelete((SMSCommand) null);
    }

    public void handleDelete(SMSCommand ett) throws Exception {
        if (this.getPermission("D")) {
            if (ett == null) {
                this.mmodel.delete(this.mselectedSMS);
                for (SMSCommand s : mselectedSMS) {
                    this.mlistSMS.remove(s);
                }
            } else {
                this.mmodel.delete(Collections.singletonList(ett));
                this.mlistSMS.remove(ett);
            }
            this.mselectedSMS = null;
            this.mtmpSMS = new SMSCommand();
            ClientMessage.logDelete();
        }
    }

    //////////////////////////////////////////
    public String getInOut(String strValue) {
        if (strValue.equals("I")) {
            return ResourceBundleUtil.getCTObjectAsString("PP_SMSCOMMAND", "in");
        } else if (strValue.equals("O")) {
            return ResourceBundleUtil.getCTObjectAsString("PP_SMSCOMMAND", "out");
        }
        return "";
    }

    public boolean vlaData(SMSCommand ent) {
        for (SMSCommand value : mlistSMS) {
            if (value.getCmdCode().equalsIgnoreCase(ent.getCmdCode())) {
                ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.ERR, ResourceBundleUtil.getCTObjectAsString("PP_SMSCOMMAND", "duplicate_code"));
                return false;
            }
        }
        return true;
    }

    public boolean vlaDataEdit(SMSCommand ent, List<SMSCommand> lsSms) {
        List<SMSCommand> lsCheckEdit = new ArrayList<SMSCommand>(lsSms);

        for (int i = 0; i < lsCheckEdit.size(); i++) {
            if (selectedIndex == i) {
                lsCheckEdit.remove(i);
            }
        }
        for (SMSCommand value : lsCheckEdit) {
            if (value.getCmdCode().equalsIgnoreCase(ent.getCmdCode())) {
                ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.ERR, ResourceBundleUtil.getCTObjectAsString("PP_SMSCOMMAND", "duplicate_code"));
                return false;
            }
        }
        return true;
    }

    public boolean isIsSelectedSMS() {
        return this.mselectedSMS != null && !this.mselectedSMS.isEmpty();
    }

    public List<SMSCommand> getMlistSMS() {
        return mlistSMS;
    }

    public void setMlistSMS(List<SMSCommand> mlistSMS) {
        this.mlistSMS = mlistSMS;
    }

    public SMSCommand getMtmpSMS() {
        return mtmpSMS;
    }

    public void setMtmpSMS(SMSCommand mtmpSMS) {
        this.mtmpSMS = mtmpSMS;
    }

    public List<SMSCommand> getMselectedSMS() {
        return mselectedSMS;
    }

    public void setMselectedSMS(List<SMSCommand> mselectedSMS) {
        this.mselectedSMS = mselectedSMS;
    }

    public List<SMSCommand> getMlistFilteredSMS() {
        return mlistFilteredSMS;
    }

    public void setMlistFilteredSMS(List<SMSCommand> mlistFilteredSMS) {
        this.mlistFilteredSMS = mlistFilteredSMS;
    }

    public SelectItem[] getMstatusOption() {
        return mstatusOption;
    }

    public void setMstatusOption(SelectItem[] mstatusOption) {
        this.mstatusOption = mstatusOption;
    }

    public SMSCommand getSelectedSMS() {
        return selectedSMS;
    }

    public void setSelectedSMS(SMSCommand selectedSMS) {
        this.selectedSMS = selectedSMS;
    }

    //    <!--validator="ValidatorAlphaNum"-->
//    <!--validatorMessage="#{PP_SMSCOMMAND.cmdCode}#{PP_COMMONS.is_not_valid}"-->

//<!--<p:commandLink id="btn_copy"-->
//<!--rendered="#{smsCommandController.isAllowInsert and not smsCommandController.isDisplayBtnConfirm}"-->
//<!--actionListener="#{smsCommandController.changeStateCopy(sms)}"-->
//<!--process="@this" styleClass="fa fa-clone"-->
//<!--update=":form_main:panel_center"/>&nbsp;&nbsp;-->
//                                <!--<p:tooltip for="btn_copy" value="#{PP_COMMONS.copy}"/>-->

//    <div class="ui-g-12 ui-md-12 ui-lg-2">
////                                <p:commandButton id="btn_del1"
////    rendered="#{smsCommandController.isAllowDelete and not smsCommandController.isDisplayBtnConfirm}"
////    actionListener="#{smsCommandController.handleDelete()}"
////    disabled="#{not smsCommandController.isSelectedSMS}" process="@this"
////    styleClass=" btn-danger" value="#{PP_COMMONS.del}"
////    update="panel_center" icon="fa fa-trash"
////    oncomplete="PF('table_sms').clearFilters();">
////                                    <p:confirm header="#{PP_COMMONS.delete_dialog_header}"
////    message="#{PP_COMMONS.confirm_delete}"/>
////                                </p:commandButton>
////                            </div>


//    <p:ajax global="false" event="rowSelectCheckbox" process="table_sms"
//    update=":form_main:pnl_control"/>
//                            <p:ajax global="false" event="rowUnselectCheckbox" process="table_sms"
//    update=":form_main:pnl_control"/>
//    <p:column selectionMode="multiple" style="width:16px;text-align:center"/>

//    rowselectionmode="checkbox"

//    <div class="ui-g-12 ui-md-12 ui-lg-2">
//                                <p:commandButton id="btn_del1"
//    rendered="#{smsCommandController.isAllowDelete and not smsCommandController.isDisplayBtnConfirm}"
//    actionListener="#{smsCommandController.handleDelete()}"
//    disabled="#{not smsCommandController.isSelectedSMS}" process="@this"
//    styleClass=" btn-danger" value="#{PP_COMMONS.del}"
//    update="panel_center" icon="fa fa-trash"
//    oncomplete="PF('table_sms').clearFilters();">
//                                    <p:confirm header="#{PP_COMMONS.delete_dialog_header}"
//    message="#{PP_COMMONS.confirm_delete}"/>
//                                </p:commandButton>
//                            </div>
}
