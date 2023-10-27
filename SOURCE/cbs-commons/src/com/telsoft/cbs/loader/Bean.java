package com.telsoft.cbs.loader;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

public class Bean {
    @JsonProperty("id")
    @Setter
    @Getter
    String id;

    @Setter
    @Getter
    @JsonProperty("info")
    @JsonSerialize(converter = BeanInfoToJson.class)
    @JsonDeserialize(converter = JsonToBeanInfo.class)
    BeanInfo info;
}
