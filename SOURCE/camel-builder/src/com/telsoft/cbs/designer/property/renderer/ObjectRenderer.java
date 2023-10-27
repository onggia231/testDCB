package com.telsoft.cbs.designer.property.renderer;

import com.l2fprod.common.propertysheet.renderer.DefaultCellRenderer;

public class ObjectRenderer extends DefaultCellRenderer {
    @Override
    protected String convertToString(Object value) {
        if (value == null)
            return "<null>";
        return String.valueOf(value);
    }
}
