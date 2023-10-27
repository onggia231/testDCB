package com.telsoft.cbs.designer.utils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParameterInfo {
    private String name;
    private Object value;
    private TYPE type;

    public enum TYPE {
        path,
        parameter
    }
}
