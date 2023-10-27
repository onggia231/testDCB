package com.telsoft.cbs.designer.converter;

public class ShortConverter implements Converter<Short> {
    @Override
    public String toString(Short value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Short parseString(String stringValue) {
        return Short.parseShort(stringValue);
    }
}
