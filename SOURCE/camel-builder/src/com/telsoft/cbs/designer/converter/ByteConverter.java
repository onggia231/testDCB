package com.telsoft.cbs.designer.converter;

public class ByteConverter implements Converter<Byte> {
    @Override
    public String toString(Byte value) {
        return value == null ? null : value.toString();
    }

    @Override
    public Byte parseString(String stringValue) {
        return Byte.parseByte(stringValue);
    }
}
