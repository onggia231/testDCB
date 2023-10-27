package vn.com.telsoft.controller;

import com.faplib.lib.ClientMessage;
import com.faplib.lib.SystemLogger;
import com.faplib.lib.TSFuncTemplate;
import com.faplib.lib.TelsoftException;
import com.faplib.lib.util.DataUtil;
import com.faplib.lib.util.JSFUtils;
import com.faplib.lib.util.MessageUtil;
import com.faplib.lib.util.ResourceBundleUtil;
import com.faplib.ws.client.ClientRequest;
import com.sun.xml.internal.ws.client.RequestContext;
import org.apache.commons.lang3.SerializationUtils;
import org.omnifaces.config.OmniFaces;
import org.omnifaces.util.Ajax;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.validate.ClientValidator;
import vn.com.telsoft.entity.Content;
import vn.com.telsoft.entity.ContentRecognize;
import vn.com.telsoft.model.LazyContentModel;
import vn.com.telsoft.model.ManageContentModel;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@ViewScoped
@Named
public class ManageContentController extends TSFuncTemplate implements Serializable {

    private static final long serialVersionUID = -594140889180286030L;

    private ManageContentModel model;

    private Content mcontent;
    private Content mcontentSelected;
    private Content mcontentSelectedLazy;
    private LazyDataModel<Content> lazyData;

    private ContentRecognize mcontentRecognize;
    private List<ContentRecognize> listContentRecognize;
    private List<ContentRecognize> listContentRecognizeSelected;
    private List<ContentRecognize> listContentRecognizeFiltered;

    private List<ContentRecognize> listInsert;
    private List<ContentRecognize> listDelete;

    private String mflagRecog;
    private String renderRecog;


    public ManageContentController() throws Exception {

        model = new ManageContentModel();
        listContentRecognize = new ArrayList<>();
        String strSQL = model.getSQLLazy();
        lazyData = new LazyContentModel(strSQL);
        lazyData.load(0, 1, null, SortOrder.ASCENDING, new HashMap<>());
        List<Content> list = ((LazyContentModel) lazyData).getDatasource();
        if (list != null && list.size() > 0) {
            mcontentSelected = ((LazyContentModel) lazyData).getFirst();
            mcontent = mcontentSelected;
            listContentRecognize = model.getRecognizeByContendId(mcontentSelected.getContentId());
        }
        renderRecog = "0";
        listDelete = new ArrayList<>();
        listInsert = new ArrayList<>();
    }

    public void onRowSelect(SelectEvent evt) throws Exception {
        this.mcontent = (Content) evt.getObject();
        mcontentSelected = mcontent;
        mcontentSelectedLazy = new Content(mcontentSelected);
        listContentRecognize = model.getRecognizeByContendId(mcontent.getContentId());
    }

    public void onRowUnselect() throws Exception {
        mcontent = new Content();
        mcontentSelectedLazy = null;
        listContentRecognize = new ArrayList<>();
    }

    public void onPageChangeLazy() throws Exception {
        if (mcontentSelected != null) {
            mcontentSelectedLazy = new Content(mcontentSelected);
        } else {
            mcontentSelected = new Content(mcontentSelectedLazy);
        }
    }

    public void onAdd() throws Exception {
        super.changeStateAdd();
        mcontent = new Content();
        listContentRecognize = new ArrayList<>();
        PrimeFaces.current().executeScript("PF('table_recognize').clearFilters();");
    }
    //////////////////////////////////////////////////////////////////////////////////

    public void changeStateEdit(Content content) throws Exception {
        super.changeStateEdit();
        mcontent = (Content) SerializationUtils.clone(content);
        listContentRecognize = model.getRecognizeByContendId(mcontent.getContentId());
        PrimeFaces.current().executeScript("PF('table_recognize').clearFilters();");
        listInsert = new ArrayList<>();
        listDelete = new ArrayList<>();
    }
    //////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////

    @Override
    public void handleOK() throws Exception {
        if (isADD || isCOPY) {
            //Check permission
            if (!getPermission("I")) {
                return;
            }
            model.insert(mcontent, listContentRecognize);
            PrimeFaces.current().executeScript("PF('table_content').clearFilters();");
            ClientMessage.logAdd();
            //Message to client
        } else if (isEDIT) {
            //Check permission
            if (!getPermission("U")) {
                return;
            }
            try {
                model.update(mcontent, listInsert, null, listDelete);
            } catch (Exception ex) {
                if (ex.getMessage().contains("ERROR_UNIQUE_CONTENT")) {
                    ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_MANAGE_CONTENT", "ERROR_UNIQUE_CONTENT"));
                    return;
                }
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

    public void handleDelete(Content ett) throws Exception {
        if (!getPermission("D")) {
            return;
        }
        model.deleteContent(ett);
        mcontentSelected = null;
        mcontent = new Content();
        ClientMessage.logDelete();
        onCancel();
    }


    public void onCancel() throws Exception {
        super.handleCancel();
        mcontent = new Content();
        listContentRecognize = new ArrayList<>();
        PrimeFaces.current().executeScript("PF('table_recognize').clearFilters();");
        listDelete = new ArrayList<>();
        listInsert = new ArrayList<>();
        renderRecog = "0";
    }

    public void onAddRecognize() throws Exception {
        mflagRecog = "add";
        renderRecog = "1";
        mcontentRecognize = new ContentRecognize();
    }

    public void onAcceptRecognize() throws Exception {
//        HashMap<String,String> hmCheck = new HashMap<>();
//        for(ContentRecognize recog : listContentRecognize){
//            hmCheck.put(recog.getKeyWord(),recog.getMatchingType());
//        }
//        String strCheck = hmCheck.get(mcontentRecognize.getKeyWord());
//        if(!strCheck.equals("")){
//          ClientMessage.logPErr("");
//        }
        for (ContentRecognize recog : listContentRecognize) {
            if (mcontentRecognize.getKeyWord().equals(recog.getKeyWord())) {
                if (mcontentRecognize.getMatchingType().equals(recog.getMatchingType())) {
                    ClientMessage.logErr(ResourceBundleUtil.getCTObjectAsString("PP_MANAGE_CONTENT", "ERROR_UNIQUE_CONTENT"));
                    return;
                }
            }
        }
        listContentRecognize.add(0, mcontentRecognize);
        listInsert.add(mcontentRecognize);
        PrimeFaces.current().executeScript("PF('table_recognize').clearFilters();");
        renderRecog = "0";
    }

    public void deleteRecognize(ContentRecognize ett) throws Exception {
        if (!model.checkData(ett.getKeyWord(), ett.getMatchingType(), ett.getContentId())) {
            listContentRecognize.remove(ett);
        } else {
            listDelete.add(ett);
            listContentRecognize.remove(ett);
        }
        for (ContentRecognize value : listInsert) {
            if (value.getKeyWord().equals(ett.getKeyWord()) && value.getMatchingType().equals(ett.getMatchingType())) {
                listInsert.remove(ett);
                break;
            }
        }
    }

    public void onCancelRecognize() throws Exception {
        renderRecog = "0";
    }

    public boolean getIsDisplayBtnConfirm() throws Exception {
        return this.isADD || this.isEDIT || this.isCOPY;
    }

    public String getRenderRecog() {
        return renderRecog;
    }

    public void setRenderRecog(String renderRecog) {
        this.renderRecog = renderRecog;
    }

    public Content getMcontent() {
        return mcontent;
    }

    public void setMcontent(Content mcontent) {
        this.mcontent = mcontent;
    }

    public LazyDataModel<Content> getLazyData() {
        return lazyData;
    }

    public void setLazyData(LazyDataModel<Content> lazyData) {
        this.lazyData = lazyData;
    }

    public Content getMcontentSelected() {
        return mcontentSelected;
    }

    public void setMcontentSelected(Content mcontentSelected) {
        this.mcontentSelected = mcontentSelected;
    }

    public ContentRecognize getMcontentRecognize() {
        return mcontentRecognize;
    }

    public void setMcontentRecognize(ContentRecognize mcontentRecognize) {
        this.mcontentRecognize = mcontentRecognize;
    }

    public List<ContentRecognize> getListContentRecognize() {
        return listContentRecognize;
    }

    public void setListContentRecognize(List<ContentRecognize> listContentRecognize) {
        this.listContentRecognize = listContentRecognize;
    }

    public List<ContentRecognize> getListContentRecognizeSelected() {
        return listContentRecognizeSelected;
    }

    public void setListContentRecognizeSelected(List<ContentRecognize> listContentRecognizeSelected) {
        this.listContentRecognizeSelected = listContentRecognizeSelected;
    }

    public List<ContentRecognize> getListContentRecognizeFiltered() {
        return listContentRecognizeFiltered;
    }

    public void setListContentRecognizeFiltered(List<ContentRecognize> listContentRecognizeFiltered) {
        this.listContentRecognizeFiltered = listContentRecognizeFiltered;
    }


}
