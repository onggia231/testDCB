package com.telsoft.cbs.designer.property.renderer;

import com.l2fprod.common.propertysheet.renderer.DefaultCellRenderer;

public class ListOfExceptionRenderer extends DefaultCellRenderer {
    @Override
    protected String convertToString(Object value) {
        return "(Click to edit)";
    }
}
