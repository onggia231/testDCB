package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.TransactedDefinition;

import javax.swing.*;

@Group("Transactional")
public class TransactedHelper extends LeafNodeHelper {

    @Override
    public NamedNode newInstance() {
        return new TransactedDefinition();
    }

    @Override
    public Class getClazz() {
        return TransactedDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.TRAFFIC, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Transacted</b>";
    }
}
