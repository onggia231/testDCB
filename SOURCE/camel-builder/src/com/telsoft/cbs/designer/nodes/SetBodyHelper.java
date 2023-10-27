package com.telsoft.cbs.designer.nodes;

import com.telsoft.util.StringEscapeUtil;
import org.apache.camel.NamedNode;
import org.apache.camel.model.SetBodyDefinition;

@Group("Message")
public class SetBodyHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new SetBodyDefinition();
    }

    @Override
    public Class getClazz() {
        return SetBodyDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        SetBodyDefinition set = (SetBodyDefinition) node;
        return "<b>Set Body </b>" + (set.getExpression() == null ? "" : StringEscapeUtil.escapeHtml(set.getExpression().getExpression()));
    }
}
