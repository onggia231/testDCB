package com.telsoft.cbs.designer.dialog;

import com.telsoft.cbs.designer.utils.Filter;
import com.telsoft.swing.JXText;
import com.telsoft.util.StringUtil;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ClassSelectorDialog extends BaseDialog {
    private final Class baseClass;

    @Getter
    @Setter
    private Class selectedClass = null;
    private List<Class> classes = new ArrayList<>();
    private JList<Class> list = null;

    @Getter
    @Setter
    private Filter classFilter = null;

    private JXText textFilter = new JXText();

    public ClassSelectorDialog(Frame parent, boolean modal, Class baseClass) {
        super(parent, modal);
        this.baseClass = baseClass;
    }

    @Override
    protected String getTitleText() {
        return "Choose class";
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setLayout(new BorderLayout());
        List<Class> classes = ClassesSelectorDialog.enumerateClasses(baseClass, classFilter);
        Collections.sort(classes, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        this.classes.addAll(classes);

        list = new JList<>();
        list.setModel(new DefaultListModel<>());
        JScrollPane scrollPane = new JScrollPane(list);
        panelContent.add(scrollPane, BorderLayout.CENTER);

        panelContent.add(textFilter, BorderLayout.NORTH);
        textFilter.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateFilter(textFilter.getText());
            }

            public void removeUpdate(DocumentEvent e) {
                updateFilter(textFilter.getText());
            }

            public void insertUpdate(DocumentEvent e) {
                updateFilter(textFilter.getText());
            }
        });
        textFilter.setText(getSelectedClassAsString());
    }

    void updateFilter(String s) {
        String[] elements = StringUtil.toStringArray(s.toLowerCase(), " ");
        DefaultListModel model = (DefaultListModel) list.getModel();
        model.clear();
        for (Class clazz : classes) {
            boolean found = true;
            String className = clazz.getName().toLowerCase();
            for (String e : elements) {
                if (!className.contains(e)) {
                    found = false;
                    break;
                }
            }
            if (found)
                model.addElement(clazz);
        }
        if (getSelectedClass() != null)
            list.setSelectedValue(getSelectedClass(), true);
    }

    @Override
    protected void onOK() {
        selectedClass = list.getSelectedValue();
        super.onOK();
    }

    public String getSelectedClassAsString() {
        if (selectedClass == null)
            return null;
        return selectedClass.getName();
    }

    public void setSelectedClassAsString(String text) throws Exception {
        if (text == null || text.length() == 0)
            setSelectedClass(null);
        else {
            try {
                Class clazz = Class.forName(text);
                setSelectedClass(clazz);
            } catch (Exception ex) {
                setSelectedClass(null);
            }
        }
    }
}
