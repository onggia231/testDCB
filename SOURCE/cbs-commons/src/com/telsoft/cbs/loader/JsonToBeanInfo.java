package com.telsoft.cbs.loader;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.telsoft.cbs.utils.PropertyUtils;

import java.lang.reflect.Field;
import java.util.Map;

public class JsonToBeanInfo extends StdConverter<Map<String, Object>, BeanInfo> {
    @Override
    public BeanInfo convert(Map<String, Object> map) {
        BeanInfo beanInfo = new BeanInfo();
        String clazz = (String) map.get("class");
        beanInfo.setBeanObject(null);
        beanInfo.setClazz(clazz);
        try {
            Map<String, Field> fields = PropertyUtils.getFields(beanInfo.getBeanObject());
            if (fields != null) {
                for (Map.Entry<String, Object> e : map.entrySet()) {
                    Field field = fields.get(e.getKey());
                    if (field != null) {
                        field.set(beanInfo.getBeanObject(), e.getValue());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return beanInfo;
    }
}
