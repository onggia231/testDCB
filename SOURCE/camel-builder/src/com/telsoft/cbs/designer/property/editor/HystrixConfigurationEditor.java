package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.BeanEditor;
import org.apache.camel.model.HystrixConfigurationDefinition;

import javax.swing.*;

public class HystrixConfigurationEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {

            BeanEditor<HystrixConfigurationDefinition> beanEditor = new BeanEditor<>(null, true, HystrixConfigurationDefinition.class);
            beanEditor.init();
            beanEditor.setBean((HystrixConfigurationDefinition) ((CBButton) this.editor).getUserObject());
            int code = beanEditor.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                Object o = getValue();
                ((CBButton) this.editor).setUserObject(beanEditor.getBean());
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
