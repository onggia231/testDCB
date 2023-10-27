package com.telsoft.cbs.designer.dialog;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.telsoft.cbs.designer.property.CamelFactory;
import com.telsoft.cbs.designer.utils.PropertyUtils;
import com.telsoft.cbs.utils.ObjectCreator;
import com.telsoft.swing.MessageBox;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;

public class BeanEditor<T> extends BaseDialog {
    private PropertySheetPanel properties = new PropertySheetPanel();

    @Getter
    private Class<T> beanClass;

    private T bean;

    public BeanEditor(Frame parent, boolean modal, Class<T> beanClass) {
        super(parent, modal);
        this.beanClass = beanClass;
        if (beanClass == null)
            throw new NullPointerException("beanClass");
    }

    public T getBean() {
        return bean;
    }

    public void setBean(T bean) {
        this.bean = bean;

        try {
            Property[] pp = PropertyUtils.getProperties(getBeanClass(), this.bean);
            properties.setProperties(pp);
        } catch (Exception e) {
            MessageBox.showMessageDialog(getParent(), e);
            properties.setProperties(new Property[0]);
        }
    }

    @Override
    protected String getTitleText() {
        return "Editor - " + getBeanClass().getName();
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setLayout(new BorderLayout());

        add(properties, BorderLayout.CENTER);

        CamelFactory.registerTo(properties);
        properties.setDescriptionVisible(true);
        properties.setSorting(true);
        properties.setEditable(true);
        properties.addPropertySheetChangeListener(evt -> {
            DefaultProperty p = (DefaultProperty) evt.getSource();
            if (getBean() == null) {
                setBean(newInstance());
            }

            try {
                Field f = (Field) p.getUserdata("field");
                f.set(getBean(), evt.getNewValue());
            } catch (Exception ex) {
                MessageBox.showMessageDialog(getParent(), ex);
            }
        });
    }

    public T newInstance() {
        try {
            return getBeanClass().newInstance();
        } catch (Exception ex) {
            return (T) ObjectCreator.create(getBeanClass());
        }
    }

    @Override
    protected void initButton() {
        JButton setNullButton = new JButton("Set Null");
        addButton(setNullButton);

        setNullButton.addActionListener(e -> {
            setBean(null);
            onOK();
        });
    }
}
