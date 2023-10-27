package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.ScriptDefinition;

@Group("Other")
public class ScriptHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new ScriptDefinition();
    }

    @Override
    public Class getClazz() {
        return ScriptDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Script</b>";
    }
}
