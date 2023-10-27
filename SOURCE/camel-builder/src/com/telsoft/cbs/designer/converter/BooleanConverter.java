package com.telsoft.cbs.designer.converter;

public class BooleanConverter implements Converter<Boolean> {
    @Override
    public String toString(Boolean value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Boolean parseString(String stringValue) {
        return Boolean.parseBoolean(stringValue);
    }
}
