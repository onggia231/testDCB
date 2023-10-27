package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.ProcessDefinition;

@Group("Process")
public class ProcessHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new ProcessDefinition();
    }

    @Override
    public Class getClazz() {
        return ProcessDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        ProcessDefinition process = (ProcessDefinition) node;
        if (process.getRef() == null)
            return "<font color='red'><b>Process</b> (No Ref)</font>";
        else
            return "<b>Process</b> (" + process.getRef() + ")";
    }
}
