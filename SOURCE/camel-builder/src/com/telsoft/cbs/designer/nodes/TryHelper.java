package com.telsoft.cbs.designer.nodes;

import com.telsoft.cbs.designer.panel.Registry;
import org.apache.camel.NamedNode;
import org.apache.camel.model.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Group("Try-Catch-Finally")
public class TryHelper extends OutputNodeHelper<TryDefinition> {
    @Override
    public String onGetText(TryDefinition node) {
        return "<b>Try</b>";
    }

    @Override
    public void insertChild(JTree tree, TryDefinition parent, NamedNode child, int index) {
        if (child instanceof CatchDefinition) {
            int i, catchIndex = -1;
            for (i = parent.getOutputs().size() - 1; i >= 0; i--) {
                if (parent.getOutputs().get(i) instanceof CatchDefinition) {
                    catchIndex = i;
                    break;
                }
            }
            if (catchIndex != -1)
                parent.getOutputs().add(catchIndex, (ProcessorDefinition<?>) child);
            else
                parent.getOutputs().add((ProcessorDefinition<?>) child);
        } else if (child instanceof FinallyDefinition) {
            parent.getOutputs().add((ProcessorDefinition<?>) child);
        } else {
            int i, catchIndex = -1;
            for (i = 0; i < parent.getOutputs().size(); i++) {
                if (parent.getOutputs().get(i) instanceof CatchDefinition || parent.getOutputs().get(i) instanceof FinallyDefinition) {
                    catchIndex = i;
                    break;
                }
            }
            if (catchIndex != -1)
                parent.getOutputs().add(catchIndex, (ProcessorDefinition<?>) child);
            else
                parent.getOutputs().add((ProcessorDefinition<?>) child);
        }
        updateTree(tree);
    }

    @Override
    public List<Class> getChildTypes(TryDefinition treeNode) {
        List<Class> list = new ArrayList<>(super.getChildTypes(treeNode));
        list.add(CatchDefinition.class);

        if (treeNode.getFinallyClause() == null) {
            list.add(FinallyDefinition.class);
        }
        return list;
    }

    @Override
    public TryDefinition newInstance() {

        TryDefinition try_ = new TryDefinition();
        NodeHelper catchHelper = Registry.getHelper(CatchDefinition.class);
        NodeHelper finallyHelper = Registry.getHelper(FinallyDefinition.class);
        NodeHelper pipelineHelper = Registry.getHelper(PipelineDefinition.class);

        NamedNode catch_ = catchHelper.newInstance();
        NamedNode finally_ = finallyHelper.newInstance();
        NamedNode pipeline = pipelineHelper.newInstance();

        try_.addOutput((ProcessorDefinition<?>) pipeline);
        try_.addOutput((ProcessorDefinition<?>) catch_);
        try_.addOutput((ProcessorDefinition<?>) finally_);
        return try_;
    }

    @Override
    public Class<TryDefinition> getClazz() {
        return TryDefinition.class;
    }

    @Override
    public boolean canInsertAt(TryDefinition parent, NamedNode child, int index) {
        return false;
    }
}