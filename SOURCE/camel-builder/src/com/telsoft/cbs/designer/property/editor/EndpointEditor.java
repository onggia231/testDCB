package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.ComponentSelectorDialog;

import javax.swing.*;

public class EndpointEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {
            ComponentSelectorDialog selector = new ComponentSelectorDialog(null, true,
                    "1".equals(getProperty().getUserdata("input")));
            selector.setUri((String) ((CBButton) this.editor).getUserObject());
            selector.init();
            int code = selector.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                Object o = getValue();
                ((CBButton) this.editor).setUserObject(selector.getUri());
                this.firePropertyChange(o, getValue());
            }
        });
    }

    public Object getValue() {
        return ((CBButton) this.editor).getUserObject();
    }

    public void setValue(Object value) {
        ((CBButton) this.editor).setUserObject(value);
    }
}
