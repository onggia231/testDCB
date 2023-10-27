package com.telsoft.cbs.designer.utils;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.telsoft.cbs.designer.converter.Converter;
import com.telsoft.cbs.designer.converter.Converters;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.util.URISupport;
import org.apache.commons.lang3.ClassUtils;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentInfo {
    @Getter
    @Setter
    String name;

    private Map schema;

    @Override
    public String toString() {
        if (schema == null)
            return name;
        Map<String, Object> props = (Map<String, Object>) schema.get("component");
        return name + " - " + props.get("description");
    }

    public Map getSchema() {
        return schema;
    }

    public void setSchema(Map schema) {
        this.schema = schema;
    }

    public Property[] getPropertyList(Map<String, ParameterInfo> parameters) {
        if (schema == null) {
            parameters.clear();
            return new Property[0];
        }
        Map<String, ParameterInfo> temp = new HashMap<>();
        temp.putAll(parameters);
        parameters.clear();

        Map<String, Map<String, String>> props = (Map<String, Map<String, String>>) schema.get("properties");
        List<Property> propertyList = new ArrayList<>();
        for (Map.Entry<String, Map<String, String>> e : props.entrySet()) {
            DefaultProperty p = new DefaultProperty();

            Map<String, String> info = e.getValue();
            p.setName(e.getKey());
            p.setDisplayName(info.get("displayName"));
            p.setMandatory(Boolean.parseBoolean(String.valueOf(info.get("required"))));
            p.setShortDescription(info.get("description"));

            Class clazz;
            try {
                clazz = ClassUtils.getClass(info.get("javaType"));
            } catch (ClassNotFoundException e1) {
                p.setEditable(false);
                clazz = Void.TYPE;
            }

            propertyList.add(p);
            p.setType(clazz);

            ParameterInfo pi = temp.get(p.getName());
            if (pi != null)
                p.setValue(pi.getValue());
            else {
                pi = new ParameterInfo();
                pi.setName(p.getName());
            }
            parameters.put(p.getName(), pi);
            pi.setType(ParameterInfo.TYPE.valueOf(info.get("kind")));
            p.setCategory(info.get("group"));
        }
        Property[] pp = new Property[propertyList.size()];
        propertyList.toArray(pp);
        return pp;
    }

    public boolean isConsumer() {
        if (schema == null)
            return true;
        Map<String, Object> props = (Map<String, Object>) schema.get("component");
        return (Boolean) props.get("consumerOnly") || !((Boolean) props.get("producerOnly"));
    }

    public boolean isProducer() {
        if (schema == null)
            return true;
        Map<String, Object> props = (Map<String, Object>) schema.get("component");
        return !((Boolean) props.get("consumerOnly")) || ((Boolean) props.get("producerOnly"));
    }

    public Map<String, ParameterInfo> extractUrl(String uri) {
        if (schema == null)
            return new HashMap<>();

        Map<String, ParameterInfo> parameters = new HashMap<>();

        try {
            URI uri_ = new URI(URISupport.normalizeUri(uri));
            String path = uri_.getAuthority();
            Map<String, Object> data = URISupport.parseParameters(uri_);

            Map<String, Map<String, String>> props = (Map<String, Map<String, String>>) schema.get("properties");

            for (Map.Entry<String, Map<String, String>> e : props.entrySet()) {
                Map<String, String> info = e.getValue();
                ParameterInfo.TYPE type = ParameterInfo.TYPE.valueOf(info.get("kind"));
                Converter converter = Converters.getConverter(info.get("javaType"));
                if (converter == null)
                    converter = Converters.defaultConverter();

                if (type == ParameterInfo.TYPE.path && path != null) {
                    ParameterInfo parameterInfo = new ParameterInfo();
                    parameterInfo.setName(e.getKey());
                    parameterInfo.setValue(converter.parseString(path));
                    parameterInfo.setType(ParameterInfo.TYPE.path);
                    parameters.put(parameterInfo.getName(), parameterInfo);
                } else if (type == ParameterInfo.TYPE.parameter) {
                    Object value = data.get(e.getKey());
                    if (value == null)
                        continue;

                    ParameterInfo parameterInfo = new ParameterInfo();
                    parameterInfo.setName(e.getKey());
                    parameterInfo.setValue(converter.parseString((String) value));
                    parameterInfo.setType(ParameterInfo.TYPE.parameter);
                    parameters.put(parameterInfo.getName(), parameterInfo);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return parameters;
    }
}
