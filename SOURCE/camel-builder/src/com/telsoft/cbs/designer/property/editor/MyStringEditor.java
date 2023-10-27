package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.telsoft.swing.JXText;

public class MyStringEditor extends StringPropertyEditor {
    public Object getValue() {
        String newValue = ((JXText) this.editor).getText();
        if ("".equals(newValue)) {
            return null;
        }
        return ((JXText) this.editor).getText();
    }
}
