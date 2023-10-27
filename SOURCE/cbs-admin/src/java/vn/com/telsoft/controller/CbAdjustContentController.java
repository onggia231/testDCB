/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.util.DataUtil;
import com.faplib.lib.util.ResourceBundleUtil;
import com.faplib.util.DateUtil;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.event.data.FilterEvent;
import org.primefaces.model.LazyDataModel;
import vn.com.telsoft.entity.*;
import vn.com.telsoft.model.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;

/**
 * @author TRIEUNV
 */
@Named
@ViewScoped
public class CbAdjustContentController extends TSFuncTemplate implements Serializable {

    private static final long serialVersionUID = -6700260726918657739L;

    private CbListModel mCbmodel;
    private List<CBList> mCbList;
    private Long listId;
    private String keyword;
    private List<CBItemContentExt> mCbItemContentExtList;
    private List<CBItemContentExt> mCbItemContentExtListFilter;
    private CBItemContentExt mselectedCbItemContentExt;
    private CbItemContentExtModel mCbItemContentExtModel;
    private CbStoreModel mCbStoreModel;
    private CbItemContentModel cbItemContentModel;
    private List<CBStore> mCbStoreList;
    private CBItemContentExt mCbItemContentExt;
    private String content;
    private CbContentModel mCbContentModel;
    private String lookupContent;
    private LazyDataModel<Content> mlistContentLazy;
    private LazyDataModel<Content> mlistContentFilterLazy;
    Content mselectedContentLazy;
    Content contentSelect;
    private Map<String, Object> filterState = new HashMap<>();


    public CbAdjustContentController() throws Exception {
        try {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String parameter = params.get("cbListId");
            if (parameter != null && !parameter.isEmpty()) {
                listId = Long.parseLong(parameter);
            }
        } catch (Exception ex) {
            listId = null;
        }
        mCbmodel = new CbListModel();
        mCbStoreModel = new CbStoreModel();
        mCbItemContentExtModel = new CbItemContentExtModel();
        mCbItemContentExt = new CBItemContentExt();
        mselectedContentLazy = new Content();
        contentSelect = new Content();
        mCbContentModel = new CbContentModel();
        cbItemContentModel = new CbItemContentModel();
        CBList cbList = new CBList();
        cbList.setStatus(1);
        cbList.setListType(1);
        mCbList = mCbmodel.getCbList(cbList);
        mCbItemContentExtList = new ArrayList<>();
        CBStore cbStore = new CBStore();
        cbStore.setStatus(1L);
        mCbStoreList = mCbStoreModel.getCBStoreAll(cbStore);
        lookupContent = "";
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void onTabChange(TabChangeEvent event) throws Exception {
        super.changeStateAdd();
        String tab = event.getTab().getId();
        if (tab.equals("tab2")) {
        }
    }

    public void delete(CBItemContentExt cbExt) throws Exception {
        cbItemContentModel.deleteCBItemContent(cbExt.getCbItemContent());
        mCbItemContentExtList.remove(cbExt);
        mCbItemContentExtListFilter = null;
        mselectedCbItemContentExt = null;
        PrimeFaces.current().executeScript("PF('tbl_cbItemContentExt').clearFilters();");
        ClientMessage.logDelete();
    }

    public void addCbItemContent() throws Exception {
        if (mCbItemContentExt.getCbItemContent().getContentId() != null) {
            if (mCbContentModel.checkCbContentDup(mCbItemContentExt.getCbItemContent().getContentId())) {
                mCbItemContentExt.getCbItemContent().setListId(listId);
                if (!cbItemContentModel.checkCbItemContentDup(mCbItemContentExt.getCbItemContent())) {
                    try {
                        mCbItemContentExtModel.insertCBItemContentExtAll(mCbItemContentExt);
                    } catch (Exception ex) {
                        if (ex.getMessage().contains("ORA-12899")) {
                            ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_ADJUSTCONTENT", "valueReasonTooLarge"));
                            return;
                        }
                        throw ex;
                    }
                    ClientMessage.log(ResourceBundleUtil.getCTObjectAsString("PP_ADJUSTCONTENT", "addSuccess"));
                    mCbItemContentExtListFilter = null;
                    mCbItemContentExt = new CBItemContentExt();
                    content = "";
                } else {
                    ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_ADJUSTCONTENT", "duplicateContent"));
                }
            } else {
                ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_ADJUSTCONTENT", "contentNotExist"));
            }
        } else {
            ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_ADJUSTCONTENT", "contentNotEmpty"));
        }
    }

    public void onFilterChange(FilterEvent filterEvent) {
        filterState = filterEvent.getFilters();
        mCbItemContentExtListFilter = (List<CBItemContentExt>) filterEvent.getData();
    }


    public void onSelectedContent(SelectEvent event) throws Exception {
        contentSelect = (Content) event.getObject();
    }

    public void selectContentLazy() throws Exception {
        mCbItemContentExt.setContentDescription(contentSelect.getContentId().toString());
        content = contentSelect.getContentDescription();
        mCbItemContentExt.getCbItemContent().setContentId(contentSelect.getContentId());
        preSearchContent();
    }

    //////////////////////////////////////////////////////////////////////////////////

    public void search(long id) throws Exception {
        mCbItemContentExtList = mCbItemContentExtModel.getCBItemContentExtAll(id, keyword);
        if (mCbItemContentExtList != null && mCbItemContentExtList.size() > 0) {
            for (CBItemContentExt dto : mCbItemContentExtList) {
                if (dto.getCbItemContent().getStoreId() == 0L) {
                    dto.setNameStore("Tất cả các kho ứng dụng");
                }
            }
        }
        mCbItemContentExtListFilter = null;
        mselectedCbItemContentExt = null;
        DataTable table = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form_main:tabMain:tbl_cbItemContentExt");
        table.setValueExpression("sortBy", null);
    }

    public void preSearchContent() throws Exception {
        lookupContent = "";
        mselectedContentLazy = null;
        mlistContentLazy = null;
        mlistContentFilterLazy = null;
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form_main:tableLazyContent");
        dataTable.setValueExpression("sortBy", null);
    }

    public void searchContent(String str) throws Exception {
        mlistContentFilterLazy = null;
        mselectedContentLazy = null;
        mlistContentLazy = new LazyContentModel(mCbContentModel.getSql(str));
        PrimeFaces.current().ajax().update("form_main:tableLazyContent");
    }

    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleDelete() throws Exception {

    }

    //////////////////////////////////////////////////////////////////////////////////
    public String getDateToStr(Date date) {
        return DateUtil.getDateTimeStr(date);
    }

    @Override
    public void handleOK() throws Exception {

    }

    @Override
    public void handleCancel() throws Exception {
    }


    //Getters


    public List<CBStore> getmCbStoreList() {
        return mCbStoreList;
    }

    public void setmCbStoreList(List<CBStore> mCbStoreList) {
        this.mCbStoreList = mCbStoreList;
    }

    public List<CBList> getmCbList() {
        return mCbList;
    }

    public void setmCbList(List<CBList> mCbList) {
        this.mCbList = mCbList;
    }

    public CbListModel getmCbmodel() {
        return mCbmodel;
    }

    public void setmCbmodel(CbListModel mCbmodel) {
        this.mCbmodel = mCbmodel;
    }

    public Long getListId() {
        return listId;
    }

    public void setListId(Long listId) {
        this.listId = listId;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public List<CBItemContentExt> getmCbItemContentExtList() {
        return mCbItemContentExtList;
    }

    public void setmCbItemContentExtList(List<CBItemContentExt> mCbItemContentExtList) {
        this.mCbItemContentExtList = mCbItemContentExtList;
    }

    public List<CBItemContentExt> getmCbItemContentExtListFilter() {
        return mCbItemContentExtListFilter;
    }

    public void setmCbItemContentExtListFilter(List<CBItemContentExt> mCbItemContentExtListFilter) {
        this.mCbItemContentExtListFilter = mCbItemContentExtListFilter;
    }

    public CBItemContentExt getMselectedCbItemContentExt() {
        return mselectedCbItemContentExt;
    }

    public void setMselectedCbItemContentExt(CBItemContentExt mselectedCbItemContentExt) {
        this.mselectedCbItemContentExt = mselectedCbItemContentExt;
    }

    public CBItemContentExt getmCbItemContentExt() {
        return mCbItemContentExt;
    }

    public void setmCbItemContentExt(CBItemContentExt mCbItemContentExt) {
        this.mCbItemContentExt = mCbItemContentExt;
    }

    public String getLookupContent() {
        return lookupContent;
    }

    public void setLookupContent(String lookupContent) {
        this.lookupContent = lookupContent;
    }

    public LazyDataModel<Content> getMlistContentLazy() {
        return mlistContentLazy;
    }

    public void setMlistContentLazy(LazyDataModel<Content> mlistContentLazy) {
        this.mlistContentLazy = mlistContentLazy;
    }

    public Content getMselectedContentLazy() {
        return mselectedContentLazy;
    }

    public void setMselectedContentLazy(Content mselectedContentLazy) {
        this.mselectedContentLazy = mselectedContentLazy;
    }

    public Content getContentSelect() {
        return contentSelect;
    }

    public void setContentSelect(Content contentSelect) {
        this.contentSelect = contentSelect;
    }


    public LazyDataModel<Content> getMlistContentFilterLazy() {
        return mlistContentFilterLazy;
    }

    public void setMlistContentFilterLazy(LazyDataModel<Content> mlistContentFilterLazy) {
        this.mlistContentFilterLazy = mlistContentFilterLazy;
    }

    public Map<String, Object> getFilterState() {
        return filterState;
    }

    public void setFilterState(Map<String, Object> filterState) {
        this.filterState = filterState;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
