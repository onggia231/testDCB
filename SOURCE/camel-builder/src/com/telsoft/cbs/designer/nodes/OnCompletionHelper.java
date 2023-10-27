package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.OnCompletionDefinition;

@Group("Other")
public class OnCompletionHelper extends OutputNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new OnCompletionDefinition();
    }

    @Override
    public Class getClazz() {
        return OnCompletionDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>On Completion</b>";
    }
}
