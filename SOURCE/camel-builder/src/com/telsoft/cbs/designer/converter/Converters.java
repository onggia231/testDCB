package com.telsoft.cbs.designer.converter;

import java.util.HashMap;
import java.util.Map;

public final class Converters {
    private static final Converter defaultConverter = new DefaultConverter();
    private static final Map<String, Converter> mapStringClassToConverter = new HashMap<>();
    private static final Map<Class, Converter> mapClassToConverter = new HashMap<>();

    static {
        register("boolean", Boolean.TYPE, new BooleanConverter());
        register("byte", Byte.TYPE, new ByteConverter());
        register("char", Character.TYPE, new CharConverter());
        register("double", Double.TYPE, new DoubleConverter());
        register("float", Float.TYPE, new FloatConverter());
        register("int", Integer.TYPE, new IntConverter());
        register("long", Long.TYPE, new LongConverter());
        register("short", Short.TYPE, new ShortConverter());
        register("java.lang.String", String.class, new StringConverter());

        register(Boolean.class.getName(), new BooleanConverter());
        register(Byte.class.getName(), new ByteConverter());
        register(Character.class.getName(), new CharConverter());
        register(Double.class.getName(), new DoubleConverter());
        register(Float.class.getName(), new FloatConverter());
        register(Integer.class.getName(), new IntConverter());
        register(Long.class.getName(), new LongConverter());
        register(Short.class.getName(), new ShortConverter());
    }

    private static void register(String javaType, Class clazz, Converter converter) {
        mapStringClassToConverter.put(javaType, converter);
        mapClassToConverter.put(clazz, converter);
    }

    private static void register(String javaType, Converter converter) {
        mapStringClassToConverter.put(javaType, converter);
    }

    public static Converter getConverter(String javaType) {
        return mapStringClassToConverter.get(javaType);
    }

    public static Converter getConverter(Class clazz) {
        return mapClassToConverter.get(clazz);
    }

    public static Converter defaultConverter() {
        return defaultConverter;
    }
}
