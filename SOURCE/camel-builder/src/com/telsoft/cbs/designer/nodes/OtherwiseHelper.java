package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.OtherwiseDefinition;

import javax.swing.*;

@Group("Choice-When-Otherwise")
public class OtherwiseHelper extends OutputNodeHelper {
    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.SWAP_CALLS, selected);
    }

    @Override
    public NamedNode newInstance() {
        return new OtherwiseDefinition();
    }

    @Override
    public Class getClazz() {
        return OtherwiseDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Otherwise</b>";
    }
}
