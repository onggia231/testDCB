package com.telsoft.cbs.domain;

import lombok.Getter;

public enum CONTENT_TYPE {
    OTHER_CONTENT("0000000001"),
    GOOGLE_CONTENT("0000000002");

    @Getter
    private final String code;

    CONTENT_TYPE(String code) {
        this.code = code;
    }
}
