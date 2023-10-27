package com.telsoft.cbs.loader;

import com.telsoft.cbs.utils.ObjectCreator;
import lombok.Getter;
import lombok.Setter;

public class BeanInfo {
    @Getter
    @Setter
    private String id;

    @Getter
    @Setter
    private String clazz;

    private Object object;

    public Object getBeanObject() throws Exception {
        if (object != null)
            return object;

        if (clazz == null)
            return null;

        object = ObjectCreator.create(Class.forName(clazz));
        return object;
    }

    public void setBeanObject(Object object) {
        this.object = object;
        if (object == null)
            this.clazz = null;
        else
            this.clazz = object.getClass().getName();
    }

    public Class getObjectClass() throws ClassNotFoundException {
        return Class.forName(clazz);
    }

    public String toString() {
        try {
            Object o = getBeanObject();
            return o.toString();
        } catch (Exception e) {
            return "Error when create object";
        }
    }
}
