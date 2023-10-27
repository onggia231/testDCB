package vn.com.telsoft.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.util.DataUtil;
import com.faplib.lib.util.ResourceBundleUtil;
import vn.com.telsoft.entity.ApParam;
import vn.com.telsoft.model.ApParamModel;
import org.apache.commons.lang.SerializationUtils;
import org.primefaces.PrimeFaces;


@ManagedBean(name = "apParamController")
@ViewScoped
public class ApParamController extends TSFuncTemplate implements Serializable {
    private static final long serialVersionUID = 5157949913560772777L;

    private ApParam apParam;
    private ApParam mselectapParam;
    private List<ApParam> listApParam;
    private List<ApParam> listApParamFiltered;
    private int indexFilter;

    public ApParamController() throws Exception {
        listApParam = DataUtil.getData(ApParamModel.class, "getApParam", new Object[]{});
        if (listApParam != null && !listApParam.isEmpty()) {
            apParam = new ApParam(listApParam.get(0));
            mselectapParam = new ApParam(apParam);
        }
        onRowSelected();

    }

    //Methods
    @Override
    public void changeStateAdd() throws Exception {
        super.changeStateAdd();
        apParam = new ApParam();
    }

    @Override
    public void changeStateCopy() throws Exception {
        super.changeStateCopy();
        apParam = new ApParam(mselectapParam);
    }

    //    @Override
//    public void changeStateEdit() throws Exception {
//        super.changeStateEdit();
//        apParam = new ApParam(mselectapParam);
//    }
    private int index;

    public void changeStateEdit(ApParam ent) throws Exception {
        super.changeStateEdit();
        apParam = (ApParam) SerializationUtils.clone(ent);
        //////
        for (int i = 0; i < listApParam.size(); i++) {
            if (listApParam.get(i).getParName().equals(ent.getParName())) {
                index = i;
            }
        }

        if (null != listApParamFiltered) {
            for (int i = 0; i < listApParamFiltered.size(); i++) {
                if (listApParamFiltered.get(i).getParName().equals(ent.getParName())) {
                    indexFilter = i;
                }
            }
        }
    }

//    @Override
//    public void changeStateDel() throws Exception {
//        super.changeStateDel();
//        apParam = new ApParam(mselectapParam);
//    }

    public void onRowSelected() {
        apParam = new ApParam(mselectapParam);
    }

    public void onRowUnSelected() {
        apParam = new ApParam();
    }

    @Override
    public void handleCancel() throws Exception {
        super.handleCancel();

        if (listApParam != null && !listApParam.isEmpty()) {
//            apParam = new ApParam(listApParam.get(0));
            if (mselectapParam != null) {
                apParam = new ApParam(mselectapParam);
            }
//            mselectapParam = new ApParam(apParam);
        }
    }

    @Override
    public void handleDelete() throws Exception {
        this.handleDelete((ApParam) null);
    }

    public void handleDelete(ApParam ent) throws Exception {
        // TODO Auto-generated method stub
        //Check permission
//            if (!getPermission("D")) {
////                return;
////            }
//////
        if (null != listApParamFiltered) {
            for (int i = 0; i < listApParamFiltered.size(); i++) {
                if (listApParamFiltered.get(i).getParName().equals(ent.getParName())) {
                    indexFilter = i;
                }
            }
        }

        DataUtil.getStringData(ApParamModel.class, "delete", new Object[]{ent.getParName()});

//            listApParam = DataUtil.getData(ApParamModel.class, "getApParam", new Object[]{});

        for (int i = 0; i < listApParam.size(); i++) {
            ApParam ett = listApParam.get(i);
            if (ett.getParName() == ent.getParName()) {
                listApParam.remove(i);
                break;
            }
        }

        if (listApParam != null && listApParam.size() > 0) {
            apParam = new ApParam(listApParam.get(0));
            mselectapParam = new ApParam(apParam);
        } else {
            apParam = new ApParam();
            mselectapParam = new ApParam();
        }

        if (null != listApParamFiltered) {
            listApParamFiltered.remove(indexFilter);
        }

        //Message to client
        ClientMessage.logDelete();
    }

//    @Override
//    public void handleDelete(ApParam ent) {
//        // TODO Auto-generated method stub
//        try {
//            //Check permission
//            if (!getPermission("D")) {
//                return;
//            }
//
//            DataUtil.getStringData(ApParamModel.class, "delete", new Object[]{apParam.getParName()});
//
//            listApParam = DataUtil.getData(ApParamModel.class, "getApParam", new Object[]{});
//
//            for (int i = 0; i < listApParam.size(); i++) {
//                ApParam ett = listApParam.get(i);
//                if (ett.getParName() == apParam.getParName()) {
//                    listApParam.remove(i);
//                    break;
//                }
//            }
//
//            if (listApParam != null && listApParam.size() > 0) {
//                apParam = new ApParam(listApParam.get(0));
//                mselectapParam = new ApParam(apParam);
//            } else {
//                apParam = new ApParam();
//                mselectapParam = new ApParam();
//            }
//
//            //Message to client
//            ClientMessage.logDelete();
//
//        } catch (Exception ex) {
//            SystemLogger.getLogger().error(ex);
//            ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.DELETE, ex.toString());
//        }
//    }

    @Override
    public void handleOK() throws Exception {
        // TODO Auto-generated method stub
        if (isADD) {
            //Check permission
            if (!getPermission("I")) {
                return;

            }
            if (!vlaData(apParam)) {
                return;
            }
            String strResult = (String) DataUtil.performAction(ApParamModel.class, "add", apParam);
            if (strResult != null && !strResult.isEmpty()) {
                apParam.setParName(strResult);
                mselectapParam = new ApParam(apParam);
            }

            listApParam.add(0, apParam);
            if (null != listApParamFiltered) {
                listApParamFiltered.add(0, apParam);
            }
            apParam = new ApParam();
//            PrimeFaces.current().executeScript("PF('table_sms').clearFilters();");
//                handleCancel();
            //Message to client
            ClientMessage.logAdd();
            handleCancel();
        } else if (isCOPY) {
            //Check permission
            if (!getPermission("I")) {
                return;

            }
//            for (ApParam p : listApParam) {
//                if (p.getParName().equals(apParam.getParName())) {
//                    ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.ADD, "Parname này đã tồn tại!");
//                    return;
//                }
//
//            }
            String strResult = (String) DataUtil.performAction(ApParamModel.class, "add", apParam);
            if (strResult != null && !strResult.isEmpty()) {
                apParam.setParName(strResult);
                mselectapParam = new ApParam(apParam);
            }

            listApParam.add(0, apParam);
            if (null != listApParamFiltered) {
                listApParamFiltered.add(0, apParam);
            }
            this.isCOPY = false;
            this.isDISABLE = true;
            apParam = new ApParam();

            onRowSelected();
            handleCancel();
            //Message to client
            ClientMessage.logAdd();
        } else if (isEDIT) {
            //Check permission
            if (!getPermission("U")) {
                return;

            }
            if (!vlaDataEdit(apParam, listApParam)) {
                return;
            }
            DataUtil.performAction(ApParamModel.class, "update", apParam);
            mselectapParam = new ApParam(apParam);
            for (int i = 0; i < listApParam.size(); i++) {
                ApParam ett = listApParam.get(i);
                if (ett.getParName().equals(apParam.getParName())) {
                    listApParam.remove(i);
                    listApParam.add(i, apParam);
                    break;
                }
            }
            if (null != listApParamFiltered) {
                listApParam.set(indexFilter, apParam);
            }
            this.isEDIT = false;
            this.isDISABLE = true;
            handleCancel();
            onRowSelected();
            //Message to client
            ClientMessage.logUpdate();
        }
    }


    public boolean vlaData(ApParam ent) {
        for (ApParam value : listApParam) {
            if (value.getParName().equalsIgnoreCase(ent.getParName())) {
                ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.ERR, ResourceBundleUtil.getCTObjectAsString("PP_APPARAM", "duplicate_parname"));
                return false;
            }
        }
        return true;
    }

    public boolean vlaDataEdit(ApParam ent, List<ApParam> lsApparam) {
        List<ApParam> lsCheckEdit = new ArrayList<ApParam>(lsApparam);

        for (int i = 0; i < lsCheckEdit.size(); i++) {
            if (index == i) {
                lsCheckEdit.remove(i);
            }
        }
        for (ApParam value : lsCheckEdit) {
            if (value.getParName().equalsIgnoreCase(ent.getParName())) {
                ClientMessage.logErr(ClientMessage.MESSAGE_TYPE.ERR, ResourceBundleUtil.getCTObjectAsString("PP_APPARAM", "duplicate_parname"));
                return false;
            }
        }
        return true;
    }


    public ApParam getApParam() {
        return apParam;
    }

    public void setApParam(ApParam apParam) {
        this.apParam = apParam;
    }

    public ApParam getMselectapParam() {
        return mselectapParam;
    }

    public void setMselectapParam(ApParam mselectapParam) {
        this.mselectapParam = mselectapParam;
    }

    public List<ApParam> getListApParam() {
        return listApParam;
    }

    public void setListApParam(List<ApParam> listApParam) {
        this.listApParam = listApParam;
    }

    public List<ApParam> getListApParamFiltered() {
        return listApParamFiltered;
    }

    public void setListApParamFiltered(List<ApParam> listApParamFiltered) {
        this.listApParamFiltered = listApParamFiltered;
    }

    //<p:commandButton id="btn_copy"
//	rendered="#{apParamController.getPermission('I')}"
//	actionListener="#{apParamController.changeStateCopy()}"
//	disabled="#{apParamController.mselectapParam eq null}"
//	process="@this :form_main:pnl_detail"
//	styleClass="btn-margin btn-cyan" value="#{PP_SHARED.copy}"
//	update=":form_main:table_app :form_main:pnl_detail :form_main:pnl_control"
//	icon="ui-icon-copy" />

//	<p:commandButton id="btn_del"
//	rendered="#{apParamController.getPermission('D')}"
//	actionListener="#{apParamController.handleDelete()}"
//	disabled="#{apParamController.mselectapParam eq null}"
//	process="@this :form_main:pnl_detail"
//	styleClass="btn-danger" style="width: 16.5%" value="#{PP_SHARED.del}"
//	oncomplete="PF('table_app').filter;"
//	icon="fa fa-trash">
//                                <p:confirm header="#{PP_COMMONS.delete_dialog_header}"
//	message="#{PP_COMMONS.confirm_delete}"/>
//                            </p:commandButton>

//    disabled="#{apParamController.isDISABLE or apParamController.isEDIT}"
}
