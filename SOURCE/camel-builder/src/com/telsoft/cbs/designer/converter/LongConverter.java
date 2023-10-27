package com.telsoft.cbs.designer.converter;

public class LongConverter implements Converter<Long> {
    @Override
    public String toString(Long value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Long parseString(String stringValue) {
        return Long.parseLong(stringValue);
    }
}
