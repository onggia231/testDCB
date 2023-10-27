package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.cloud.ServiceCallDefinition;

import javax.swing.*;

@Group("Other")
public class ServiceCallHelper extends LeafNodeHelper {

    @Override
    public NamedNode newInstance() {
        return new ServiceCallDefinition();
    }

    @Override
    public Class getClazz() {
        return ServiceCallDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.ACCESS_TIME, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        ServiceCallDefinition set = (ServiceCallDefinition) node;
        return "<b>Call</b> " + set.getLabel();
    }
}
