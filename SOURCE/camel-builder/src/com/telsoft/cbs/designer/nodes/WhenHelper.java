package com.telsoft.cbs.designer.nodes;

import com.telsoft.util.StringEscapeUtil;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import org.apache.camel.NamedNode;
import org.apache.camel.model.WhenDefinition;

import javax.swing.*;

@Group("Choice-When-Otherwise")
public class WhenHelper extends OutputNodeHelper {
    protected static String description(WhenDefinition when) {
        StringBuilder sb = new StringBuilder();
        if (when.getExpression() != null) {
            String language = when.getExpression().getLanguage();
            if (language != null) {
                sb.append(language).append(":");
            }
            sb.append(when.getExpression().getLabel());
        }
        return sb.toString();
    }

    @Override
    public Icon getIcon(NamedNode node, boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getIcon(node, GoogleMaterialDesignIcons.CHECK, selected);
    }

    @Override
    public NamedNode newInstance() {
        return new WhenDefinition();
    }

    @Override
    public Class getClazz() {
        return WhenDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        WhenDefinition when = (WhenDefinition) node;
        return "<b>When</b>(" + StringEscapeUtil.escapeHtml(description(when)) + ")";
    }
}
