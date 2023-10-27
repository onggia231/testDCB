package com.telsoft.cbs.loader;

import com.fasterxml.jackson.databind.util.StdConverter;
import com.telsoft.cbs.utils.PropertyUtils;

import java.util.HashMap;
import java.util.Map;

public class BeanInfoToJson extends StdConverter<BeanInfo, Map> {
    @Override
    public Map convert(BeanInfo beanInfo) {
        Map map = new HashMap();
        map.put("class", beanInfo.getClazz());
        try {
            Map<String, Object> values = PropertyUtils.getFieldValues(beanInfo.getBeanObject());
            if (values != null) {
                map.putAll(values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }
}
