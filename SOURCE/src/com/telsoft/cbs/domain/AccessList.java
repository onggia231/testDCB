package com.telsoft.cbs.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.util.List;

@Getter
@Setter
@Data
@Builder
public class AccessList {
    Long listId;
    String name;
    LIST_ACCESS_TYPE access_type;
    String description;
    boolean enabled;
    LIST_TYPE type;

    List<Item> items;
    boolean cached;


    public boolean checkAccess(Connection connection, String store_id, String subject) throws CBException {
        return LIST_TYPE.checkAccess(connection, this, store_id, subject);
    }
}
