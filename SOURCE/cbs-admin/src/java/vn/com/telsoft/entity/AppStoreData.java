package vn.com.telsoft.entity;

import java.io.Serializable;

public class AppStoreData implements Serializable {

    private static final long serialVersionUID = -8247538328778112607L;
    private CBStore cbStore;
    private CBStoreAttr cbStoreAttr;

    public AppStoreData() {
        this.cbStore = new CBStore();
        this.cbStoreAttr = new CBStoreAttr();
    }

    public AppStoreData(AppStoreData appStoreData) {
        this.cbStore = appStoreData.getCbStore();
        this.cbStoreAttr = appStoreData.getCbStoreAttr();
    }

    public CBStore getCbStore() {
        return cbStore;
    }

    public void setCbStore(CBStore cbStore) {
        this.cbStore = cbStore;
    }

    public CBStoreAttr getCbStoreAttr() {
        return cbStoreAttr;
    }

    public void setCbStoreAttr(CBStoreAttr cbStoreAttr) {
        this.cbStoreAttr = cbStoreAttr;
    }
}
