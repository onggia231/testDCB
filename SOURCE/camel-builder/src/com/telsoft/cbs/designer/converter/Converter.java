package com.telsoft.cbs.designer.converter;

public interface Converter<T> {
    String toString(T value);

    T parseString(String stringValue);
}
