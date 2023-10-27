package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.ClassesSelectorDialog;

import javax.swing.*;
import java.util.List;

public class ListOfExceptionEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {
            ClassesSelectorDialog classSelector = new ClassesSelectorDialog(null, true, Exception.class);
            try {
                classSelector.setSelectedClassesAsString((List) ((CBButton) this.editor).getUserObject());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            classSelector.init();
            int code = classSelector.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                Object o = getValue();
                ((CBButton) this.editor).setUserObject(classSelector.getSelectedClassesAsString());
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
