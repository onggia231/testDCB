package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.FromDefinition;

import javax.swing.*;

@Group("Endpoint")
public class FromHelper extends LeafNodeHelper {
    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.INPUT, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        FromDefinition from = (FromDefinition) node;
        return "<b>From </b>" + from.getLabel();
    }

    @Override
    public NamedNode newInstance() {
        return new FromDefinition();
    }

    @Override
    public Class getClazz() {
        return FromDefinition.class;
    }

    @Override
    public boolean isCloneable() {
        return false;
    }

    @Override
    public boolean isRemovable() {
        return false;
    }
}
