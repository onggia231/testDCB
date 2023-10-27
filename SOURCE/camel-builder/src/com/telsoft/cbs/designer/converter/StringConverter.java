package com.telsoft.cbs.designer.converter;

public class StringConverter implements Converter<String> {
    @Override
    public String toString(String value) {
        return value;
    }

    @Override
    public String parseString(String stringValue) {
        return stringValue;
    }
}
