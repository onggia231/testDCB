package com.telsoft.cbs.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Isdn extends Item {
    String isdn;

    @Override
    public boolean match(String store_id, String subject) {
        if (getStore_id() == null || this.getStore_id().equals(store_id))
            return isdn.equals(subject);
        return false;
    }
}
