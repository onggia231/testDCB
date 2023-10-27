package com.telsoft.cbs.utils;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

public class ObjectCreator {
    public static <T> T create(Class<T> clazz) {
        Objenesis objenesis = new ObjenesisStd(); // or ObjenesisSerializer
        return (T) objenesis.newInstance(clazz);
    }
}
