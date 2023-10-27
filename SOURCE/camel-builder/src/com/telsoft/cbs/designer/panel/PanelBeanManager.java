package com.telsoft.cbs.designer.panel;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.telsoft.cbs.designer.dialog.AddBeanDialog;
import com.telsoft.cbs.designer.utils.BeanItemRenderer;
import com.telsoft.cbs.designer.utils.PropertyUtils;
import com.telsoft.cbs.loader.BeanInfo;
import com.telsoft.swing.JXPanel;
import com.telsoft.swing.MessageBox;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.lang.reflect.Field;

@Service
public class PanelBeanManager extends CBPanel implements ListDataListener {
    @Autowired
    private FormMain formMain;

    @Autowired
    private FileEditor fileEditor;

    @Autowired
    private BeanManager beanManager;
    private JButton buttonAdd = new JButton("New");
    private JButton buttonRemove = new JButton("Delete");

    private PropertySheetPanel properties = new PropertySheetPanel();

    public PanelBeanManager() {
        super();
        setLayout(new BorderLayout());
    }

    public void init() {
        PanelBean panelBean = new PanelBean(formMain, beanManager, null, null);
        add(panelBean, BorderLayout.CENTER);
        beanManager.getModel().addListDataListener(this);
    }

    @Override
    public void enterSimulationMode() {
        buttonAdd.setEnabled(false);
        buttonRemove.setEnabled(false);
        properties.setEditable(false);
    }

    @Override
    public void releaseSimulationMode() {
        buttonAdd.setEnabled(true);
        buttonRemove.setEnabled(true);
        properties.setEditable(true);
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        fileEditor.setModified();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        fileEditor.setModified();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        fileEditor.setModified();
    }

    public void updatePropertySheet(BeanInfo beanInfo) {
        if (beanInfo == null) {
            properties.setProperties(new Property[0]);
            return;
        }
        try {
            Class clazz = beanInfo.getObjectClass();
            Object o = beanInfo.getBeanObject();
            Property[] pp = PropertyUtils.getProperties(clazz, o);
            properties.setProperties(pp);
        } catch (Exception e) {
            properties.setProperties(new Property[0]);
        }
    }

    public class PanelBean extends JXPanel {
        @Getter
        private final BeanManager beanManager;
        @Getter
        JPanel panelButton = new JPanel();
        @Getter
        private JList<BeanInfo> list = new JList<>();

        public PanelBean(Frame container, BeanManager beanManager, Class baseClass, String selectedId) {
            this.beanManager = beanManager;

            setLayout(new GridBagLayout());
            list.setCellRenderer(new BeanItemRenderer());

            list.setModel(getBeanManager().getModel());

            if (selectedId != null && selectedId.length() > 0) {
                BeanInfo selected = getBeanManager().getBeans().get(selectedId);
                if (selected != null) {
                    list.setSelectedValue(selected, true);
                }
            }

            JPanel panelBean = new JPanel();
            panelBean.setLayout(new BorderLayout());
            JScrollPane scrollPane = new JScrollPane(list);
            panelBean.add(scrollPane, BorderLayout.CENTER);

            panelBean.add(panelButton, BorderLayout.SOUTH);

            panelButton.setLayout(new GridBagLayout());
            panelButton.add(buttonAdd, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));
            panelButton.add(buttonRemove, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2));

            add(panelBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2));

            properties.setDescriptionVisible(true);
            add(properties, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2));

            buttonRemove.addActionListener(e -> {
                if (list.getSelectedValue() != null) {
                    getBeanManager().getModel().removeElement(list.getSelectedValue());
                }
            });

            buttonAdd.addActionListener(e -> {
                AddBeanDialog addBeanDialog = new AddBeanDialog(container, true);
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
                    list.setSelectedValue(beanInfo, true);
                }
            });

            list.addListSelectionListener(e -> {
                if (e.getValueIsAdjusting())
                    return;

                BeanInfo beanInfo = ((JList<BeanInfo>) e.getSource()).getSelectedValue();
                updatePropertySheet(beanInfo);
            });

            properties.addPropertySheetChangeListener(e -> {
                DefaultProperty p = (DefaultProperty) e.getSource();
                BeanInfo beanInfo = list.getSelectedValue();
                if (beanInfo == null)
                    return;

                try {
                    Object obj = beanInfo.getBeanObject();
                    Field f = (Field) p.getUserdata("field");
                    f.set(obj, e.getNewValue());
                    list.updateUI();
                    fileEditor.setModified();
                } catch (Exception ex) {
                    MessageBox.showMessageDialog(formMain, ex);
                }
            });
        }
    }
}
