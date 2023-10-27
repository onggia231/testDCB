package com.telsoft.cbs.designer.dialog;

import javax.swing.*;
import java.awt.*;

public class AddBeanDialog extends BaseDialog {
    private String id;
    private Class beanType;

    private JTextField textId = new JTextField();
    private JButton textClass = new JButton();
    private Class baseClass;

    public AddBeanDialog(Frame parent, boolean modal) {
        super(parent, modal);
    }

    @Override
    protected String getTitleText() {
        return "New bean";
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setLayout(new GridBagLayout());

        GridBagConstraints gcbLabel = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2);
        GridBagConstraints gcbControl = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2);

        panelContent.add(new Label("Id"), gcbLabel);
        panelContent.add(textId, gcbControl);

        gcbLabel.gridy++;
        gcbControl.gridy++;

        panelContent.add(new Label("Class"), gcbLabel);
        panelContent.add(textClass, gcbControl);


        textClass.addActionListener(e -> {
            try {
                ClassSelectorDialog classSelector = new ClassSelectorDialog((Frame) AddBeanDialog.this.getParent(), true, getBaseClass());
                classSelector.setSelectedClassAsString(textClass.getText());
                classSelector.init();
                int code = classSelector.showDialog();
                if (code == JOptionPane.OK_OPTION) {
                    textClass.setText(classSelector.getSelectedClassAsString());
                }
            } catch (Exception e1) {
                showMessage(e1);
            }
        });
        setSize(300, 120);
    }

    @Override
    protected void onOK() {
        id = textId.getText();
        try {
            beanType = Class.forName(textClass.getText());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        super.onOK();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Class getBeanType() {
        return beanType;
    }

    public void setBeanType(Class beanType) {
        this.beanType = beanType;
    }

    public Class getBaseClass() {
        return baseClass;
    }

    public void setBaseClass(Class baseClass) {
        this.baseClass = baseClass;
    }
}
