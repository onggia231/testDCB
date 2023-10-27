package com.telsoft.cbs.designer.utils;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PropertyUtils {
    public static Property[] getProperties(Class clazz, Object o) throws IllegalAccessException {
        if (clazz == Object.class)
            return null;

        if (clazz.isPrimitive())
            return null;

        Set<Field> fields = ReflectionUtils.getAllFields(clazz);

        List<DefaultProperty> propertyList = new ArrayList<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;

            if (Modifier.isTransient(field.getModifiers()))
                continue;

            if (field.getType().isArray())
                continue;

            DefaultProperty p = new DefaultProperty();
            p.setName(field.getName());
            p.setDisplayName(field.getName());
            p.setShortDescription(field.getName());
            p.setType(field.getType());
            if (!field.isAccessible()) {
                field.setAccessible(true);
            }
            if (o != null)
                p.setValue(field.get(o));
            p.setUserdata("field", field);
            propertyList.add(p);

//            Property[] subProperties = getProperties(p.getChildType(), p.getValue());
//            if (subProperties != null) {
//                p.addSubProperties(subProperties);
//            }
        }
        Property[] pp = new Property[propertyList.size()];
        propertyList.toArray(pp);
        return pp;
    }
}
