package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.PipelineDefinition;

@Group("Other")
public class PipelineHelper extends OutputNodeHelper {
    @Override
    public String onGetText(NamedNode node) {
        return "<b>Pipeline</b>";
    }

    @Override
    public NamedNode newInstance() {
        return new PipelineDefinition();
    }

    @Override
    public Class getClazz() {
        return PipelineDefinition.class;
    }
}
