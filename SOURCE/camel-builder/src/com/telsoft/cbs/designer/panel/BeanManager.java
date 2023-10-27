package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.loader.BeanInfo;
import com.telsoft.cbs.utils.ObjectCreator;
import lombok.Getter;
import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Endpoint;
import org.apache.camel.support.SimpleRegistry;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class BeanManager {
    private final BeanManagerModel listModel = new BeanManagerModel();
    @Getter
    private Map<String, BeanInfo> beans = new HashMap<>();
    private List<BeanInfo> beanList = new ArrayList<>();

    private AtomicBoolean updating = new AtomicBoolean(false);

    public boolean contains(String id) {
        return beans.containsKey(id);
    }

    public <T> T get(String id) throws Exception {
        BeanInfo beanInfo = beans.get(id);
        if (beanInfo == null)
            return null;

        return (T) beanInfo.getBeanObject();
    }

    public BeanManagerModel getModel() {
        return listModel;
    }

    public BeanInfo create(String id, Class type) throws Exception {
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setBeanObject(ObjectCreator.create(type));
        beanInfo.setId(id);

        getModel().addElement(beanInfo);
        return beanInfo;
    }

    public void set(String id, Object o) throws Exception {
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setBeanObject(o);
        beanInfo.setId(id);
        getModel().addElement(beanInfo);
    }

    public <T> T getOrCreate(String id, Class<T> type) throws Exception {
        if (contains(id)) {
            return get(id);
        } else {
            return (T) create(id, type).getBeanObject();
        }
    }

    public org.apache.camel.spi.Registry createRegistry(CamelContext context) throws Exception {
        org.apache.camel.spi.Registry registry = new SimpleRegistry();
        for (Map.Entry<String, BeanInfo> e : getBeans().entrySet()) {
            Object o = e.getValue().getBeanObject();
            if (o instanceof CamelContextAware) {
                ((CamelContextAware) o).setCamelContext(context);
            } else if (o instanceof Endpoint) {
                ((Endpoint) o).setCamelContext(context);
            }
            registry.bind(e.getKey(), o);
        }
        return registry;
    }

    public Map<String, BeanInfo> getBeans(Class baseClass) {
        Map<String, BeanInfo> beans = new HashMap<>();
        for (Map.Entry<String, BeanInfo> e : getBeans().entrySet()) {
            try {
                if (baseClass == null || baseClass.isAssignableFrom(e.getValue().getBeanObject().getClass()))
                    beans.put(e.getKey(), e.getValue());
            } catch (Exception ignored) {

            }
        }
        return beans;
    }

    public void remove(BeanInfo beanInfo) {
        if (beanInfo != null) {
            getModel().removeElement(beanInfo);
        }
    }

    public class BeanManagerModel extends AbstractListModel<BeanInfo> {
        @Override
        public int getSize() {
            return beans.size();
        }

        @Override
        public BeanInfo getElementAt(int index) {
            return beanList.get(index);
        }

        public void removeElement(BeanInfo beanInfo) {
            int index = beanList.indexOf(beanInfo);
            boolean rv = beanList.remove(beanInfo);
            beans.remove(beanInfo.getId());
            if (index >= 0) {
                fireIntervalRemoved(this, index, index);
            }
        }

        public void addElement(BeanInfo beanInfo) {
            int index = beanList.size();
            beans.put(beanInfo.getId(), beanInfo);
            beanList.add(beanInfo);
            fireIntervalAdded(this, index, index);
        }

        public void clear() {
            beans.clear();
            beanList.clear();
        }

        public void endUpdate() {
            updating.set(false);
        }

        public void beginUpdate() {
            updating.set(true);
        }

        @Override
        protected void fireContentsChanged(Object source, int index0, int index1) {
            if (!updating.get())
                super.fireContentsChanged(source, index0, index1);
        }

        @Override
        protected void fireIntervalAdded(Object source, int index0, int index1) {
            if (!updating.get())
                super.fireIntervalAdded(source, index0, index1);
        }

        @Override
        protected void fireIntervalRemoved(Object source, int index0, int index1) {
            if (!updating.get())
                super.fireIntervalRemoved(source, index0, index1);
        }

        public void update() {
            super.fireContentsChanged(this, -1, -1);
        }
    }
}
