package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.SystemConfig;
import com.faplib.util.DateUtil;
import com.telsoft.cbs.domain.CBTypeMoMt;
import com.telsoft.cbs.domain.CHANNEL_TYPE;
import org.apache.commons.lang.StringUtils;
import org.primefaces.component.datatable.DataTable;
import vn.com.telsoft.entity.CBMtHistory;
import vn.com.telsoft.entity.CBRequest;
import vn.com.telsoft.entity.CBStore;
import vn.com.telsoft.entity.CBSubscriber;
import vn.com.telsoft.model.CBRequestModel;
import vn.com.telsoft.model.CbStoreModel;
import vn.com.telsoft.util.DateUtils;

import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Named
@ViewScoped
public class SearchHisController implements Serializable {

    private CBRequest mtmpRequest;
    private List<CBRequest> mlstRequest;
    private List<CBRequest> mlstRequestRefund;
    private List<CBMtHistory> mlstMtMoHistory;
    private List<CBStore> mlstStore;
    private CBRequestModel cbRequestModel;
    private CbStoreModel cbStoreModel;
    private CBSubscriber cbSubscriber;

    public SearchHisController() throws Exception {
        mlstRequest = new ArrayList<>();
        cbRequestModel = new CBRequestModel();
        cbStoreModel = new CbStoreModel();
        mtmpRequest = new CBRequest();
        setFromAndToDate();
        initCombobox();
    }

    private void initCombobox() throws Exception {
        CBStore store = new CBStore();
        store.setStatus(1L);
        mlstStore = cbStoreModel.getCBStoreAll(store);
    }

    private void setFromAndToDate() {
        Calendar cal = Calendar.getInstance();
        // set from date
        cal.add(Calendar.DAY_OF_MONTH, -7);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        mtmpRequest.setFromDate(cal.getTime());
        //set to date
        Calendar cal2 = Calendar.getInstance();
        cal2.set(Calendar.HOUR_OF_DAY, 23);
        cal2.set(Calendar.MINUTE, 59);
        cal2.set(Calendar.SECOND, 59);
        cal2.set(Calendar.MILLISECOND, 999);
        mtmpRequest.setToDate(cal2.getTime());
    }

    public boolean validate() {
        if (mtmpRequest.getFromDate().after(mtmpRequest.getToDate())) {
            ClientMessage.logErr("Từ ngày phải nhỏ hơn đến ngày");
            return true;
        }
        String limitDayToSearch = SystemConfig.getConfig("LimitDayToSearch");
        if(!StringUtils.isEmpty(limitDayToSearch)) {
            try{
                long llimitDay = Long.parseLong(limitDayToSearch);
                if(llimitDay < DateUtils.getDateDiff(mtmpRequest.getFromDate(), mtmpRequest.getToDate(), TimeUnit.DAYS)){
                    ClientMessage.logErr("Khoảng thời gian tìm kiếm phải nhỏ hơn " + limitDayToSearch + " ngày");
                    return true;
                }
            }catch (Exception ex){
                System.out.println("Error config LimitDayToSearch: " + ex.getMessage());
                ClientMessage.logErr("Lỗi cấu hình hệ thống");
                return true;
            }

        }

        return false;
    }

    public void handSearch() throws Exception {
        if (validate()) {
            return;
        }
        mlstRequest = cbRequestModel.searchRequest(mtmpRequest);
        cbSubscriber = cbRequestModel.getSubscriberInfo(mtmpRequest.getIsdn());
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form_main:tabHis:tableRequest");
        dataTable.reset();
    }

    public void handSearchRefund() throws Exception {
        if (validate()) {
            return;
        }
        mlstRequestRefund = cbRequestModel.searchRequestRefund(mtmpRequest);
        cbSubscriber = cbRequestModel.getSubscriberInfo(mtmpRequest.getIsdn());
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form_main:tabHis:tableRequestRefund");
        dataTable.reset();
    }

    public void handSearchMoMt() throws Exception {
        if (validate()) {
            return;
        }
        mlstMtMoHistory = cbRequestModel.searchMtMoHistory(mtmpRequest);
        cbSubscriber = cbRequestModel.getSubscriberInfo(mtmpRequest.getIsdn());
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form_main:tabHis:tableMo");
        dataTable.reset();
    }

    public String getStoreNameByCode(String code) {
        for (CBStore store : mlstStore) {
            if (store.getStoreCode().equals(code)) {
                return store.getName();
            }
        }
        return "";
    }

    //Getter, setter
    public CBRequest getMtmpRequest() {
        return mtmpRequest;
    }

    public void setMtmpRequest(CBRequest mtmpRequest) {
        this.mtmpRequest = mtmpRequest;
    }

    public List<CBRequest> getMlstRequest() {
        return mlstRequest;
    }

    public void setMlstRequest(List<CBRequest> mlstRequest) {
        this.mlstRequest = mlstRequest;
    }

    public List<CBStore> getMlstStore() {
        return mlstStore;
    }

    public void setMlstStore(List<CBStore> mlstStore) {
        this.mlstStore = mlstStore;
    }

    public List<CBRequest> getMlstRequestRefund() {
        return mlstRequestRefund;
    }

    public void setMlstRequestRefund(List<CBRequest> mlstRequestRefund) {
        this.mlstRequestRefund = mlstRequestRefund;
    }

    public List<CBMtHistory> getMlstMtMoHistory() {
        return mlstMtMoHistory;
    }

    public void setMlstMtMoHistory(List<CBMtHistory> mlstMtMoHistory) {
        this.mlstMtMoHistory = mlstMtMoHistory;
    }

    public CHANNEL_TYPE[] getChannelType() {
        return CHANNEL_TYPE.values();
    }

    public CBTypeMoMt[] getMoMtType() {
        return CBTypeMoMt.values();
    }

    public CBSubscriber getCbSubscriber() {
        return cbSubscriber;
    }

    public void setCbSubscriber(CBSubscriber cbSubscriber) {
        this.cbSubscriber = cbSubscriber;
    }
}
