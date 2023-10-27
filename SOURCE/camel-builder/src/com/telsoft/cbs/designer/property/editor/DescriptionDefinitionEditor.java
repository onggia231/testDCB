package com.telsoft.cbs.designer.property.editor;

import com.l2fprod.common.propertysheet.editor.StringPropertyEditor;
import com.l2fprod.common.util.LookAndFeelTweaks;
import com.telsoft.swing.JXText;
import org.apache.camel.model.DescriptionDefinition;

public class DescriptionDefinitionEditor extends StringPropertyEditor {
    public void initEditor() {
        this.editor = new JXText();
        ((JXText) this.editor).setBorder(LookAndFeelTweaks.EMPTY_BORDER);
    }

    public Object getValue() {
        DescriptionDefinition descriptionDefinition = new DescriptionDefinition();
        descriptionDefinition.setText(((JXText) this.editor).getText());
        return descriptionDefinition;
    }

    public void setValue(Object value) {
        if (value == null) {
            ((JXText) this.editor).setText("");
        } else {
            DescriptionDefinition descriptionDefinition = (DescriptionDefinition) value;
            ((JXText) this.editor).setText(descriptionDefinition.getText());
        }

    }
}
