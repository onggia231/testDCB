package com.telsoft.cbs.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public abstract class Item {
    String store_id;
    String reason;
    Date issueTime;

    public abstract boolean match(String store_id, String subject);
}
