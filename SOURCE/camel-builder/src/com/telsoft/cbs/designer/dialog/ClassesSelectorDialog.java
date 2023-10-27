package com.telsoft.cbs.designer.dialog;

import com.telsoft.cbs.designer.utils.Filter;
import lombok.Getter;
import lombok.Setter;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClassesSelectorDialog extends BaseDialog {
    private final static Map<Class, List<Class>> classCache = new HashMap<>();
    private final Class baseClass;
    private JList<Class> list = new JList<>(new DefaultListModel<>());
    private JList<Class> listSelected = new JList<>(new DefaultListModel<>());
    @Getter
    @Setter
    private List<Class> selectedClasses = null;
    private AtomicBoolean updating = new AtomicBoolean(false);
    @Getter
    @Setter
    private Filter classFilter = null;

    public ClassesSelectorDialog(JFrame parent, boolean modal, Class baseClass) {
        super(parent, modal);
        this.baseClass = baseClass;
    }

    public static List<Class> enumerateClasses(Class baseClass, Filter classFilter) {
        Class key = baseClass;
        if (key == null) {
            key = NULL.class;
        }

        List<Class> result = classCache.get(key);
        if (result == null) {
            Reflections reflections = new Reflections(new SubTypesScanner(false));
            Set<Class<?>> responses;
            if (baseClass == null)
                responses = reflections.getSubTypesOf(Object.class);
            else
                responses = reflections.getSubTypesOf(baseClass);


            result = new ArrayList<>();
            if (baseClass != null)
                responses.add(baseClass);

            for (Class clazz : responses) {
                if (Modifier.isAbstract(clazz.getModifiers()))
                    continue;

                if (Modifier.isInterface(clazz.getModifiers()))
                    continue;

                String clazzName = clazz.getName();
                if (clazzName.startsWith("com.sun.") ||
                        clazzName.startsWith("com.oracle.") ||
                        clazzName.startsWith("com.ibm.") ||
                        clazzName.startsWith("jrockit.") ||
                        clazzName.startsWith("oracle.jrockit.") ||
                        clazzName.startsWith("sunw.") ||
                        clazzName.startsWith("javax.") ||
                        clazzName.startsWith("java.awt.") ||
                        clazzName.startsWith("org.omg.") ||
                        clazzName.startsWith("org.springframework.") ||
                        clazzName.startsWith("javafx.") ||
                        clazzName.startsWith("javassist.") ||
                        clazzName.startsWith("javax.swing.plaf.") ||
                        clazzName.contains(".management.") ||
                        clazzName.startsWith("sun."))
                    continue;

                if (clazzName.contains("$")) {
                    continue;
                }

                if (clazzName.contains("internal")) {
                    continue;
                }

                try {
                    if (clazz.isMemberClass())
                        continue;
                } catch (Throwable ex) {
                    continue;
                }

                result.add(clazz);
            }
            classCache.put(key, result);
        }

        List<Class> filteredResult = new ArrayList<>();
        if (classFilter != null) {
            for (Class clazz : result) {
                if (!classFilter.filter(clazz))
                    continue;
                filteredResult.add(clazz);
            }
        } else {
            filteredResult.addAll(result);
        }
        return filteredResult;
    }

    @Override
    protected String getTitleText() {
        return "Choose classes";
    }

    @Override
    protected void initContent(JPanel panelContent) {
        panelContent.setLayout(new GridLayout(2, 1, 5, 5));
        List<Class> classes = enumerateClasses(baseClass, classFilter);
        Collections.sort(classes, (o1, o2) -> o1.getName().compareTo(o2.getName()));


        for (Class clazz : classes)
            ((DefaultListModel) list.getModel()).addElement(clazz);

        if (getSelectedClasses() != null) {
            for (Class clazz : getSelectedClasses()) {
                ((DefaultListModel) list.getModel()).removeElement(clazz);
                ((DefaultListModel) listSelected.getModel()).addElement(clazz);
            }
        }

        JScrollPane scrollPane = new JScrollPane(list);
        panelContent.add(scrollPane);
        JScrollPane scrollPaneSelected = new JScrollPane(listSelected);
        panelContent.add(scrollPaneSelected);

        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            if (updating.get())
                return;

            updating.set(true);
            try {
                Class selected = list.getSelectedValue();
                ((DefaultListModel) listSelected.getModel()).addElement(selected);
                ((DefaultListModel) list.getModel()).removeElement(selected);
                listSelected.updateUI();
                list.updateUI();
            } finally {
                updating.set(false);
            }
        });

        listSelected.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;
            if (updating.get())
                return;
            updating.set(true);
            try {
                Class selected = listSelected.getSelectedValue();
                ((DefaultListModel) list.getModel()).addElement(selected);
                ((DefaultListModel) listSelected.getModel()).removeElement(selected);
                listSelected.updateUI();
                list.updateUI();
            } finally {
                updating.set(false);
            }
        });
    }

    @Override
    protected void onOK() {
        List l = Arrays.asList(((DefaultListModel) listSelected.getModel()).toArray());
        selectedClasses = new ArrayList<>(l);
        super.onOK();
    }

    public List<String> getSelectedClassesAsString() {
        if (getSelectedClasses() == null)
            return null;

        List<String> zzz = new ArrayList<>();
        for (Class clazz : getSelectedClasses()) {
            zzz.add(clazz.getName());
        }
        return zzz;
    }

    public void setSelectedClassesAsString(List<String> classes) throws Exception {
        if (classes == null) {
            setSelectedClasses(null);
        } else {
            List<Class> classList = new ArrayList<>();
            for (String s : classes) {
                classList.add(Class.forName(s));
            }
            setSelectedClasses(classList);
        }
    }

    private static abstract class NULL {
    }
}
