package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.OnFallbackDefinition;

import javax.swing.*;

@Group("Circuit Breaker")
public class OnFallbackHelper extends OutputNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new OnFallbackDefinition();
    }

    @Override
    public Class getClazz() {
        return OnFallbackDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.DONE_ALL, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>On Fallback</b>";
    }
}
