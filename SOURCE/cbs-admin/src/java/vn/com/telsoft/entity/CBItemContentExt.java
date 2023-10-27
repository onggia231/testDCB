package vn.com.telsoft.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author TRIEUNV
 */
public class CBItemContentExt implements Serializable {

    private static final long serialVersionUID = 6143052210547277959L;

    private CBItemContent cbItemContent;
    private String nameStore;
    private String contentDescription;

    public CBItemContentExt() {
        cbItemContent = new CBItemContent();
    }

    public CBItemContent getCbItemContent() {
        return cbItemContent;
    }

    public void setCbItemContent(CBItemContent cbItemContent) {
        this.cbItemContent = cbItemContent;
    }

    public String getNameStore() {
        return nameStore;
    }

    public void setNameStore(String nameStore) {
        this.nameStore = nameStore;
    }

    public String getContentDescription() {
        return contentDescription;
    }

    public void setContentDescription(String contentDescription) {
        this.contentDescription = contentDescription;
    }
}
