package com.telsoft.cbs.designer.converter;

public class IntConverter implements Converter<Integer> {
    @Override
    public String toString(Integer value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Integer parseString(String stringValue) {
        return Integer.parseInt(stringValue);
    }
}
