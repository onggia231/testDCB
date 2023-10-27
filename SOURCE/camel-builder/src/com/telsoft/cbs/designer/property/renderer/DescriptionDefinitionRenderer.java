package com.telsoft.cbs.designer.property.renderer;

import com.l2fprod.common.propertysheet.renderer.DefaultCellRenderer;
import org.apache.camel.model.DescriptionDefinition;

public class DescriptionDefinitionRenderer extends DefaultCellRenderer {
    @Override
    protected String convertToString(Object value) {
        DescriptionDefinition descriptionDefinition = (DescriptionDefinition) value;
        if (descriptionDefinition == null)
            return "";
        return descriptionDefinition.getText();
    }
}
