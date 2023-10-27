package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.BeanSelectorDialog;
import com.telsoft.cbs.designer.panel.BeanManager;

import javax.swing.*;

public class BeanStringEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {
            BeanSelectorDialog beanSelector = new BeanSelectorDialog(null, true);

            beanSelector.setBeanManager((BeanManager) getProperty().getUserdata("beanManager"));
            beanSelector.setBaseClass((Class) getProperty().getUserdata("baseClass"));
            beanSelector.setRefId((String) ((CBButton) this.editor).getUserObject());
            beanSelector.init();
            int code = beanSelector.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                Object o = getValue();
                ((CBButton) this.editor).setUserObject(beanSelector.getRefId());
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
