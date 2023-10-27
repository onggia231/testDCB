package com.telsoft.cbs.designer.nodes;

import com.telsoft.cbs.designer.panel.Registry;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.*;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class RouteHelper extends OutputNodeHelper<RouteDefinition> {
    @Override
    public Icon getIcon(RouteDefinition node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.SETTINGS, selected);
    }

    @Override
    public String onGetText(RouteDefinition node) {
        return "<b>Route</b> (" + node.getId() + ")";
    }

    @Override
    public int getIndex(RouteDefinition parent, NamedNode child) {
        if (child instanceof FromDefinition) {
            return 0;
        } else {
            return super.getIndex((ProcessorDefinition) parent, child) + (parent.getInput() == null ? 0 : 1);
        }
    }

    @Override
    public void insertChild(JTree tree, RouteDefinition parent, NamedNode child, int index) {
        if (child instanceof FromDefinition) {
            parent.setInput((FromDefinition) child);
        } else {
            if (index == -1)
                parent.getOutputs().add((ProcessorDefinition<?>) child);
            else {
                index = index - 1;
                if (index < 0) index = 0;
                parent.getOutputs().add(index, (ProcessorDefinition<?>) child);
            }
        }
        updateTree(tree);
    }

    @Override
    public NamedNode getChildAt(RouteDefinition parent, int childIndex) {
        if (parent.getInput() != null) {
            if (childIndex == 0)
                return parent.getInput();
            else
                return parent.getOutputs().get(childIndex - 1);
        } else {
            return parent.getOutputs().get(childIndex);
        }
    }

    @Override
    public int getChildCount(RouteDefinition parent) {
        return (parent.getInput() == null ? 0 : 1) + parent.getOutputs().size();
    }

    @Override
    public List<Class> getChildTypes(RouteDefinition treeNode) {
        if (treeNode.getInput() == null) {
            return Arrays.asList(FromDefinition.class);
        } else {
            List<Class> classes = super.getChildTypes(treeNode);
            classes.add(OnExceptionDefinition.class);
            classes.add(OnCompletionDefinition.class);
            return classes;
        }
    }

    @Override
    public RouteDefinition newInstance() {
        RouteDefinition route = new RouteDefinition();
        NodeHelper fromHelper = Registry.getHelper(FromDefinition.class);
        NamedNode from = fromHelper.newInstance();
        route.setInput((FromDefinition) from);
        return route;
    }

    @Override
    public Class<RouteDefinition> getClazz() {
        return RouteDefinition.class;
    }

    @Override
    public void removeChild(JTree tree, RouteDefinition parent, NamedNode child) {
        if (child instanceof FromDefinition) {
            parent.setInput(null);
            updateTree(tree);
        } else {
            int i = parent.getOutputs().indexOf(child);
            if (i >= 0) {
                parent.getOutputs().remove(i);
                updateTree(tree);
            }
        }
    }

    @Override
    public boolean canInsertAt(RouteDefinition parent, NamedNode child, int index) {
        if (child instanceof FromDefinition)
            return false;

        return (index >= 1 && index <= parent.getOutputs().size());
    }
}
