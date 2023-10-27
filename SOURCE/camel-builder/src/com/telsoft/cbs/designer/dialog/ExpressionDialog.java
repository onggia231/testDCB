package com.telsoft.cbs.designer.dialog;

import com.telsoft.cbs.designer.utils.CamelHelper;
import com.telsoft.cbs.designer.utils.ExpressionRenderer;
import lombok.Getter;
import lombok.Setter;
import org.apache.camel.CamelContext;
import org.apache.camel.model.language.ExpressionDefinition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Set;

public class ExpressionDialog extends BaseDialog {

    @Getter
    @Setter
    private ExpressionDefinition definition;
    private JComboBox<Class<? extends ExpressionDefinition>> comboLanguage = new JComboBox<>();
    private JTextArea textExpression = new JTextArea();

    public ExpressionDialog(Frame parent, boolean modal) {
        super(parent, modal);
    }

    @Override
    protected String getTitleText() {
        return "Expression";
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setBorder(new EmptyBorder(5, 5, 5, 5));
        panelContent.setLayout(new BorderLayout(5, 5));
        panelContent.add(comboLanguage, BorderLayout.NORTH);
        panelContent.add(new JScrollPane(textExpression), BorderLayout.CENTER);
        comboLanguage.setRenderer(new ExpressionRenderer());

        try {
            CamelContext context = CamelHelper.createCamelContext();
            Set<Class> map = CamelHelper.findListLanguage(context);
            for (Class clazz : map)
                comboLanguage.addItem(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (definition != null) {
            comboLanguage.setSelectedItem(definition.getClass());
            textExpression.setText(definition.getExpression());
        }
    }

    @Override
    protected void onOK() {
        definition = createExpression();
        super.onOK();
    }

    private ExpressionDefinition createExpression() {
        Class<? extends ExpressionDefinition> clazz = (Class<? extends ExpressionDefinition>) comboLanguage.getSelectedItem();
        try {
            ExpressionDefinition expressionDefinition = clazz.newInstance();
            expressionDefinition.setExpression(textExpression.getText());
            return expressionDefinition;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
