package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.OnExceptionDefinition;

@Group("Other")
public class OnExceptionHelper extends OutputNodeHelper {
    protected static String description(OnExceptionDefinition onException) {
        return onException.getExceptions() + "";
    }

    @Override
    public NamedNode newInstance() {
        return new OnExceptionDefinition();
    }

    @Override
    public Class getClazz() {
        return OnExceptionDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        OnExceptionDefinition onException = (OnExceptionDefinition) node;
        return "<b>OnException</b>(" + description(onException) + ")";
    }
}
