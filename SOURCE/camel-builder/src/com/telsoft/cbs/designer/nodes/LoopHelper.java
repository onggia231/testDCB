package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.LoopDefinition;

import javax.swing.*;

@Group("Loop")
public class LoopHelper extends OutputNodeHelper {
    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.LOOP, selected);
    }


    @Override
    public String onGetText(NamedNode node) {
        return "<b>Loop</b>";
    }

    @Override
    public NamedNode newInstance() {
        return new LoopDefinition();
    }

    @Override
    public Class getClazz() {
        return LoopDefinition.class;
    }
}
