package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.ThrowExceptionDefinition;

public class ThrowExceptionHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new ThrowExceptionDefinition();
    }

    @Override
    public Class getClazz() {
        return ThrowExceptionDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        ThrowExceptionDefinition throwException = (ThrowExceptionDefinition) node;
        return "<b>Throw</b> " + (throwException.getExceptionType() == null ? "" : throwException.getExceptionType());
    }
}
