package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.FinallyDefinition;

import javax.swing.*;

@Group("Try-Catch-Finally")
public class FinallyHelper extends OutputNodeHelper {
    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.DONE_ALL, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Finally</b>";
    }

    @Override
    public NamedNode newInstance() {
        return new FinallyDefinition();
    }

    @Override
    public Class getClazz() {
        return FinallyDefinition.class;
    }
}
