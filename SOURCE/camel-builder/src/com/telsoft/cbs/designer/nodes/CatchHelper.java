package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.CatchDefinition;

@Group("Try-Catch-Finally")
public class CatchHelper extends OutputNodeHelper {

    @Override
    public String onGetText(NamedNode node) {
        CatchDefinition catch_ = (CatchDefinition) node;
        return "<b>Catch</b> " + (catch_.getExceptions() == null ? "" : catch_.getExceptions());
    }

    @Override
    public NamedNode newInstance() {
        return new CatchDefinition();
    }

    @Override
    public Class getClazz() {
        return CatchDefinition.class;
    }
}
