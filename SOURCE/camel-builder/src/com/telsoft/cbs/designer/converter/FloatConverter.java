package com.telsoft.cbs.designer.converter;

public class FloatConverter implements Converter<Float> {
    @Override
    public String toString(Float value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Float parseString(String stringValue) {
        return Float.parseFloat(stringValue);
    }
}
