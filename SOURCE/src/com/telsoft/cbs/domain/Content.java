package com.telsoft.cbs.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class Content extends Item {
    MATCHING_TYPE matchingType;
    String keyword;
    Boolean caseSensitive;

    @Override
    public boolean match(String store_id, String subject) {

        if (getStore_id() == null || getStore_id().equals(store_id)) {
            switch (getMatchingType()) {
                case CONTAINS:
                    if (caseSensitive)
                        return subject.contains(keyword);
                    else
                        return subject.toUpperCase().contains(keyword.toUpperCase());
                case EQUALS:
                    if (caseSensitive)
                        return subject.equals(keyword);
                    else
                        return subject.equalsIgnoreCase(keyword);
                case START_WITH:
                    if (caseSensitive)
                        return subject.startsWith(keyword);
                    else
                        return subject.toUpperCase().startsWith(keyword.toUpperCase());
                case END_WITH:
                    if (caseSensitive)
                        return subject.endsWith(keyword);
                    else
                        return subject.toUpperCase().endsWith(keyword.toUpperCase());
                case MATCH_REGEX:
                    if (caseSensitive)
                        return subject.matches(keyword);
                    else {
                        if (keyword.startsWith("(?i)"))
                            return subject.matches(keyword);
                        else
                            return subject.matches("(?i)" + keyword);
                    }
                default:
                    return false;
            }
        } else
            return false;
    }
}
