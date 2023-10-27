package com.telsoft.cbs.designer.nodes;

import org.apache.camel.NamedNode;
import org.apache.camel.model.CircuitBreakerDefinition;
import org.apache.camel.model.OnFallbackDefinition;
import org.apache.camel.model.ProcessorDefinition;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Group("Circuit Breaker")
public class CircuitBreakerHelper extends OutputNodeHelper<CircuitBreakerDefinition> {
    @Override
    public String onGetText(CircuitBreakerDefinition node) {
        return "<b>Circuit Breaker</b>";
    }

    @Override
    public void insertChild(JTree tree, CircuitBreakerDefinition parent, NamedNode child, int index) {
        if (child instanceof OnFallbackDefinition) {
            parent.setOnFallback((OnFallbackDefinition) child);
            parent.getOutputs().add((OnFallbackDefinition) child);
            updateTree(tree);
        } else {
            int i, fallbackIndex = -1;
            for (i = 0; i < parent.getOutputs().size(); i++) {
                if (parent.getOutputs().get(i) instanceof OnFallbackDefinition) {
                    fallbackIndex = i;
                    break;
                }
            }
            if (fallbackIndex != -1)
                parent.getOutputs().add(fallbackIndex, (ProcessorDefinition<?>) child);
            else
                parent.getOutputs().add((ProcessorDefinition<?>) child);
            updateTree(tree);
        }

    }

    @Override
    public List<Class> getChildTypes(CircuitBreakerDefinition treeNode) {
        List<Class> list = new ArrayList<>(super.getChildTypes(treeNode));
        if (treeNode.getOnFallback() == null) {
            list.add(OnFallbackDefinition.class);
        }
        return list;
    }

    @Override
    public CircuitBreakerDefinition newInstance() {
        return new CircuitBreakerDefinition();
    }

    @Override
    public Class<CircuitBreakerDefinition> getClazz() {
        return CircuitBreakerDefinition.class;
    }

    @Override
    public void removeChild(JTree tree, CircuitBreakerDefinition parent, NamedNode child) {

        if (child instanceof OnFallbackDefinition) {
            parent.setOnFallback(null);
        }

        int i = parent.getOutputs().indexOf(child);
        if (i >= 0) {
            parent.getOutputs().remove(i);
            updateTree(tree);
        }
    }
}
