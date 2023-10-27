//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.ComboBoxPropertyEditor;

public class EnumPropertyEditor extends ComboBoxPropertyEditor {
    public EnumPropertyEditor() {
    }

    public void initEditor() {
        Class clazz = this.getType();
        if (clazz.isEnum()) {
            Object[] values = clazz.getEnumConstants();
            this.setAvailableValues(values);
        }

    }

    public Object getValue() {
        Object obj = super.getValue();
        return obj == null ? this.getProperty().getValue() : Enum.valueOf(this.getType(), String.valueOf(obj));
    }

    public void setValue(Object value) {
        if (value == null) {
            super.setValue(this.getProperty().getValue());
        } else {
            super.setValue(value.toString());
        }

    }

    public boolean canHandle(Class type) {
        return type.isEnum();
    }
}
