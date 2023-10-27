package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class RoutesHelper extends BasedNodeHelper<RoutesDefinition> {
    @Override
    public Icon getIcon(RoutesDefinition node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.DEVICE_HUB, selected);
    }

    @Override
    public String onGetText(RoutesDefinition node) {
        return "<b>Routes</b> (" + node.getRoutes().size() + " router" + (node.getRoutes().size() > 1 ? "s" : "") + ")";
    }

    @Override
    public int getIndex(RoutesDefinition parent, NamedNode child) {
        return parent.getRoutes().indexOf(child);
    }

    @Override
    public void insertChild(JTree tree, RoutesDefinition parent, NamedNode child, int index) {
        if (child instanceof RouteDefinition) {
            if (index == -1)
                parent.getRoutes().add((RouteDefinition) child);
            else
                parent.getRoutes().add(index, (RouteDefinition) child);
            updateTree(tree);
        }

    }

    @Override
    public boolean canInsertAt(RoutesDefinition parent, NamedNode child, int index) {
        return index >= 0 && index < getChildCount(parent);
    }

    @Override
    public NamedNode getChildAt(RoutesDefinition parent, int childIndex) {
        return parent.getRoutes().get(childIndex);
    }

    @Override
    public int getChildCount(RoutesDefinition parent) {
        return parent.getRoutes().size();
    }

    @Override
    public List<Class> getChildTypes(RoutesDefinition treeNode) {
        return Arrays.asList(RouteDefinition.class);
    }

    @Override
    public RoutesDefinition newInstance() {
        return new RoutesDefinition();
    }

    @Override
    public Class<RoutesDefinition> getClazz() {
        return RoutesDefinition.class;
    }

    @Override
    public boolean isCloneable() {
        return false;
    }

    @Override
    public void removeChild(JTree tree, RoutesDefinition parent, NamedNode child) {
        if (child instanceof RouteDefinition) {
            int i = parent.getRoutes().indexOf(child);
            if (i >= 0) {
                parent.getRoutes().remove(i);
                updateTree(tree);
            }
        }
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
}
