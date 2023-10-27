package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.ClassSelectorDialog;

import javax.swing.*;

public class ExceptionEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {
            ClassSelectorDialog classSelector = new ClassSelectorDialog(null, true, Exception.class);
            try {
                classSelector.setSelectedClassAsString((String) ((CBButton) this.editor).getUserObject());
                classSelector.init();
                int code = classSelector.showDialog();
                if (code == JOptionPane.OK_OPTION) {
                    Object o = getValue();
                    ((CBButton) this.editor).setUserObject(classSelector.getSelectedClassAsString());
                    this.firePropertyChange(o, getValue());
                }
            } catch (Exception e1) {
                e1.printStackTrace();
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
