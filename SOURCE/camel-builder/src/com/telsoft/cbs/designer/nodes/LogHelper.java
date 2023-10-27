package com.telsoft.cbs.designer.nodes;

import com.telsoft.util.StringEscapeUtil;
import org.apache.camel.NamedNode;
import org.apache.camel.model.LogDefinition;

@Group("Other")
public class LogHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new LogDefinition();
    }

    @Override
    public Class getClazz() {
        return LogDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        LogDefinition log = (LogDefinition) node;
        return "<b>Log </b>" + StringEscapeUtil.escapeHtml(log.getMessage());
    }
}
