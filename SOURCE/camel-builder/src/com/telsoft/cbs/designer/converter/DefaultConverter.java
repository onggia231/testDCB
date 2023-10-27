package com.telsoft.cbs.designer.converter;

public class DefaultConverter implements Converter {
    @Override
    public String toString(Object value) {
        return String.valueOf(value);
    }

    @Override
    public Object parseString(String stringValue) {
        return stringValue;
    }
}
