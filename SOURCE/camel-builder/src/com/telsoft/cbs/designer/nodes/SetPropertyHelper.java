package com.telsoft.cbs.designer.nodes;

import com.telsoft.util.StringEscapeUtil;
import org.apache.camel.NamedNode;
import org.apache.camel.model.SetPropertyDefinition;

@Group("Message")
public class SetPropertyHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new SetPropertyDefinition();
    }

    @Override
    public Class getClazz() {
        return SetPropertyDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        SetPropertyDefinition set = (SetPropertyDefinition) node;
        if (set.getName() == null || set.getName().length() == 0) {
            return "<font color='red'><b>Set Property</b></font>(No property name)";
        } else {
            return "<b>Set Property</b>(" + set.getName() + "=" + (set.getExpression() == null ? "" : StringEscapeUtil.escapeHtml(set.getExpression().getExpression()) + ")");
        }
    }
}
