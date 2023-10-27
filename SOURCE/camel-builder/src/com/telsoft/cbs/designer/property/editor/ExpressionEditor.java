package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.cbs.designer.dialog.ExpressionDialog;
import org.apache.camel.model.language.ExpressionDefinition;

import javax.swing.*;

public class ExpressionEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new CBButton();
        ((CBButton) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
        ((CBButton) this.editor).setText("(Click to edit)");
        ((CBButton) this.editor).addActionListener(e -> {


            ExpressionDialog expressionDialog = new ExpressionDialog(null, true);
            expressionDialog.setDefinition((ExpressionDefinition) ((CBButton) this.editor).getUserObject());
            expressionDialog.init();
            int code = expressionDialog.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                Object o = getValue();
                ((CBButton) this.editor).setUserObject(expressionDialog.getDefinition());
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
