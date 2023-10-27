package com.telsoft.cbs.designer.converter;

public class DoubleConverter implements Converter<Double> {
    @Override
    public String toString(Double value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Double parseString(String stringValue) {
        return Double.parseDouble(stringValue);
    }
}
