package com.telsoft.cbs.designer.nodes;

import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.RollbackDefinition;

import javax.swing.*;

@Group("Transactional")
public class RollbackHelper extends LeafNodeHelper {

    @Override
    public NamedNode newInstance() {
        return new RollbackDefinition();
    }

    @Override
    public Class getClazz() {
        return RollbackDefinition.class;
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.ARROW_BACK, selected);
    }

    @Override
    public String onGetText(NamedNode node) {
        return "<b>Rollback</b>";
    }
}
