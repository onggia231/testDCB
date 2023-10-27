/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import vn.com.telsoft.entity.CBSubscriber;
import vn.com.telsoft.model.SearchISDNModel;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

/**
 * @author TUNGLM, TELSOFT
 */
@Named
@ViewScoped
public class SearchISDNController implements Serializable {

    private static final long serialVersionUID = -966335920557516015L;

    private SearchISDNModel mmodel;
    private String isdn;
    private CBSubscriber mtmpSubscriber;
    private List<CBSubscriber> listSubScriber;
    private CBSubscriber subscriberSelected;
    private boolean bDISPLAY = false;

    public SearchISDNController() throws Exception {
        mmodel = new SearchISDNModel();
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void handleSearch() throws Exception {
        listSubScriber = mmodel.getListSubscriberInfo(isdn);
    }

    public void handleView(CBSubscriber cbSubscriber) throws Exception {
        mtmpSubscriber = mmodel.getSubscriberInfo(isdn, cbSubscriber.getId());
    }
    //////////////////////////////////////////////////////////////////////////////////

    //Getters, Setters
    public String getIsdn() {
        return isdn;
    }

    public void setIsdn(String isdn) {
        this.isdn = isdn;
    }

    public CBSubscriber getMtmpSubscriber() {
        return mtmpSubscriber;
    }

    public void setMtmpSubscriber(CBSubscriber mtmpSubscriber) {
        this.mtmpSubscriber = mtmpSubscriber;
    }

    public List<CBSubscriber> getListSubScriber() {
        return listSubScriber;
    }

    public void setListSubScriber(List<CBSubscriber> listSubScriber) {
        this.listSubScriber = listSubScriber;
    }

    public CBSubscriber getSubscriberSelected() {
        return subscriberSelected;
    }

    public void setSubscriberSelected(CBSubscriber subscriberSelected) {
        this.subscriberSelected = subscriberSelected;
    }

    public boolean isbDISPLAY() {
        return bDISPLAY;
    }

    public void setbDISPLAY(boolean bDISPLAY) {
        this.bDISPLAY = bDISPLAY;
    }
}
