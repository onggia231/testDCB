package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.ProcessorDefinition;

import javax.swing.*;

public abstract class OutputNodeHelper<T extends ProcessorDefinition> extends ProcessorHelper<T> {
    @Override
    public int getIndex(ProcessorDefinition parent, NamedNode child) {
        return parent.getOutputs().indexOf(child);
    }

    @Override
    public void insertChild(JTree tree, T parent, NamedNode child, int index) {
        if (index >= 0 && index < parent.getOutputs().size())
            parent.getOutputs().add(index, child);
        else
            parent.getOutputs().add(child);
        updateTree(tree);
    }

    @Override
    public boolean canInsertAt(T parent, NamedNode child, int index) {
        return (index >= 0 && index < parent.getOutputs().size());
    }

    @Override
    public NamedNode getChildAt(T parent, int childIndex) {
        return (NamedNode) parent.getOutputs().get(childIndex);
    }

    @Override
    public int getChildCount(T parent) {
        return parent.getOutputs().size();
    }

    @Override
    public void removeChild(JTree tree, T parent, NamedNode child) {
        int i = parent.getOutputs().indexOf(child);
        if (i >= 0) {
            parent.getOutputs().remove(i);
            updateTree(tree);
        }
    }
}
