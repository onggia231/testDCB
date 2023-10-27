package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.RemovePropertyDefinition;

@Group("Message")
public class RemovePropertyHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new RemovePropertyDefinition();
    }

    @Override
    public Class getClazz() {
        return RemovePropertyDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        RemovePropertyDefinition set = (RemovePropertyDefinition) node;
        if (set.getPropertyName() == null || set.getPropertyName().length() == 0) {
            return "<font color='red'><b>Remove property</b></font>(No property name)";
        } else {
            return "<b>Remove property</b>(" + set.getPropertyName() + ")";
        }
    }
}
