package com.telsoft.cbs.designer.converter;

public class CharConverter implements Converter<Character> {
    @Override
    public String toString(Character value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Character parseString(String stringValue) {
        return stringValue == null ? null : stringValue.charAt(0);
    }
}
