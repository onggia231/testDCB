package com.telsoft.cbs.designer.nodes;

import com.telsoft.util.StringEscapeUtil;
import org.apache.camel.NamedNode;
import org.apache.camel.model.SetHeaderDefinition;

@Group("Message")
public class SetHeaderHelper extends LeafNodeHelper {
    @Override
    public NamedNode newInstance() {
        return new SetHeaderDefinition();
    }

    @Override
    public Class getClazz() {
        return SetHeaderDefinition.class;
    }

    @Override
    public String onGetText(NamedNode node) {
        SetHeaderDefinition set = (SetHeaderDefinition) node;
        if (set.getName() == null || set.getName().length() == 0) {
            return "<font color='red'><b>Set Header</b></font>(No header name)";
        } else {
            return "<b>Set Header</b>(" + set.getName() + "=" + (set.getExpression() == null ? "" : StringEscapeUtil.escapeHtml(set.getExpression().getExpression()) + ")");
        }
    }
}
