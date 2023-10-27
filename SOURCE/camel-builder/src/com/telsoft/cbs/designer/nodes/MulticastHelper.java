package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.MulticastDefinition;

@Group("Other")
public class MulticastHelper extends OutputNodeHelper {
    @Override
    public String onGetText(NamedNode node) {
        return "<b>Multicast</b>";
    }

    @Override
    public NamedNode newInstance() {
        return new MulticastDefinition();
    }

    @Override
    public Class getClazz() {
        return MulticastDefinition.class;
    }
}
