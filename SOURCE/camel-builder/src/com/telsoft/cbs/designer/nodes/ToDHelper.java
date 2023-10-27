package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.ToDynamicDefinition;

import javax.swing.*;

@Group("Endpoint")
public class ToDHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new ToDynamicDefinition();
    }

    @Override
    public Class getClazz() {
        return ToDynamicDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.PUBLISH, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        ToDynamicDefinition toD = (ToDynamicDefinition) node;
        return "<b>ToD</b> " + toD.getLabel();
    }
}
