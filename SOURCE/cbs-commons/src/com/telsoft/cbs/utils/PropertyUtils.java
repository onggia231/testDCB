package com.telsoft.cbs.utils;

import org.reflections.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PropertyUtils {
    public static Map<String, Object> getFieldValues(Object o) throws IllegalAccessException {
        if (o == null)
            return null;

        Class clazz = o.getClass();

        if (clazz == Object.class)
            return null;

        if (clazz.isPrimitive())
            return null;

        Set<Field> fields = ReflectionUtils.getAllFields(clazz);

        Map<String, Object> mapValues = new HashMap<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;

            if (Modifier.isTransient(field.getModifiers()))
                continue;

            if (field.getType().isArray() || field.getType().isEnum())
                continue;

            if (!field.isAccessible())
                field.setAccessible(true);

            mapValues.put(field.getName(), field.get(o));
        }
        return mapValues;
    }

    public static Map<String, Field> getFields(Object o) {
        if (o == null)
            return null;

        Class clazz = o.getClass();

        if (clazz == Object.class)
            return null;

        if (clazz.isPrimitive())
            return null;

        Set<Field> fields = ReflectionUtils.getAllFields(clazz);

        Map<String, Field> mapValues = new HashMap<>();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()))
                continue;

            if (Modifier.isTransient(field.getModifiers()))
                continue;

            if (field.getType().isArray() || field.getType().isEnum())
                continue;

            if (!field.isAccessible())
                field.setAccessible(true);

            mapValues.put(field.getName(), field);
        }
        return mapValues;
    }
}
