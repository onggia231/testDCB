package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.StopDefinition;

import javax.swing.*;

@Group("Other")
public class StopHelper extends LeafNodeHelper {

    @Override
    public NamedNode newInstance() {
        return new StopDefinition();
    }

    @Override
    public Class getClazz() {
        return StopDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.CANCEL, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Stop</b>";
    }
}
