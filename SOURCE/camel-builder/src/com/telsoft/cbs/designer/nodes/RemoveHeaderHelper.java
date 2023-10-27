package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.RemoveHeaderDefinition;

@Group("Message")
public class RemoveHeaderHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new RemoveHeaderDefinition();
    }

    @Override
    public Class getClazz() {
        return RemoveHeaderDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        RemoveHeaderDefinition set = (RemoveHeaderDefinition) node;
        if (set.getHeaderName() == null || set.getHeaderName().length() == 0) {
            return "<font color='red'><b>Remove Header</b></font>(No header name)";
        } else {
            return "<b>Remove Header</b>(" + set.getHeaderName() + ")";
        }
    }
}
