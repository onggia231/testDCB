package com.telsoft.cbs.designer.dialog;

import com.telsoft.cbs.designer.panel.BeanManager;
import com.telsoft.cbs.designer.utils.BeanItemRenderer;
import com.telsoft.cbs.loader.BeanInfo;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class BeanSelectorDialog extends BaseDialog {
    @Setter
    @Getter
    BeanManager beanManager;

    @Setter
    @Getter
    String refId;

    @Setter
    @Getter
    Class baseClass;

    private JList<BeanInfo> list = new JList<>(new DefaultListModel<>());

    public BeanSelectorDialog(Frame parent, boolean modal) {
        super(parent, modal);
    }

    @Override
    protected String getTitleText() {
        return "Select an object";
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setLayout(new BorderLayout());

        list.setCellRenderer(new BeanItemRenderer());

        Map<String, BeanInfo> beans = getBeanManager().getBeans(baseClass);
        for (BeanInfo beanInfo : beans.values()) {
            ((DefaultListModel) list.getModel()).addElement(beanInfo);
        }

        if (refId != null && refId.length() > 0) {
            BeanInfo selected = getBeanManager().getBeans().get(refId);
            if (selected != null) {
                list.setSelectedValue(selected, true);
            }
        }

        JScrollPane scrollPane = new JScrollPane(list);
        panelContent.add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void initButton() {
        super.initButton();
        JButton buttonAdd = new JButton("New");
        addButton(buttonAdd);

        buttonAdd.addActionListener(e -> {
            AddBeanDialog addBeanDialog = new AddBeanDialog((Frame) this.getParent(), true);
            addBeanDialog.setBaseClass(baseClass);
            addBeanDialog.init();
            int code = addBeanDialog.showDialog();
            if (code == JOptionPane.OK_OPTION) {
                BeanInfo beanInfo = null;
                try {
                    beanInfo = beanManager.create(addBeanDialog.getId(), addBeanDialog.getBeanType());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                ((DefaultListModel) list.getModel()).addElement(beanInfo);
                list.setSelectedValue(beanInfo, true);
            }
        });
    }

    @Override
    protected void onOK() {
        super.onOK();
        BeanInfo selected = list.getSelectedValue();
        setRefId(selected == null ? null : selected.getId());
    }
}
