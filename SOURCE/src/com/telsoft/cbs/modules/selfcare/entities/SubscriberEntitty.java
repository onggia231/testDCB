package com.telsoft.cbs.modules.selfcare.entities;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

public class SubscriberEntitty {




    @Getter
    @Setter
    private long id;

    @Getter
    @Setter
    private String isdn;

    @Getter
    @Setter
    private int status;

    @Getter
    @Setter
    private int vasgateStatus;

    @Getter
    @Setter
    private int subType;

    @Getter
    @Setter
    private Date processTime;

    @Getter
    @Setter
    private int retry;

    @Getter
    @Setter
    private int retried;

    @Getter
    @Setter
    private String reason;

    @Getter
    @Setter
    private int storeId;

    @Getter
    @Setter
    private String url;

}
