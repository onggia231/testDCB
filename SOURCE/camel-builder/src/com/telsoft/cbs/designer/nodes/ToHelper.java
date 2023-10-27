package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.ToDefinition;

import javax.swing.*;

@Group("Endpoint")
public class ToHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new ToDefinition();
    }

    @Override
    public Class getClazz() {
        return ToDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.PUBLISH, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        ToDefinition to = (ToDefinition) node;
        return "<b>To</b> " + to.getLabel();
    }
}
