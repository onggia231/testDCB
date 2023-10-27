package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.DelayDefinition;

import javax.swing.*;

@Group("Other")
public class DelayHelper extends LeafNodeHelper {

    @Override
    public NamedNode newInstance() {
        return new DelayDefinition();
    }

    @Override
    public Class getClazz() {
        return DelayDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.ACCESS_TIME, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        DelayDefinition set = (DelayDefinition) node;
        return "<b>Delay</b> " + (set.getExpression() == null ? "" : set.getExpression().getExpression());
    }
}
