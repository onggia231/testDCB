//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.cbs.designer.property;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorFactory;
import com.l2fprod.common.propertysheet.datatype.EditorCallback.ObjectHolder;
import com.l2fprod.common.propertysheet.datatype.ListString;
import com.l2fprod.common.propertysheet.datatype.PasswordString;
import com.l2fprod.common.propertysheet.datatype.TextAreaString;
import com.l2fprod.common.propertysheet.editor.*;
import com.telsoft.util.LogUtil;

import java.awt.*;
import java.beans.PropertyEditor;
import java.io.File;
import java.util.*;
import java.util.Map.Entry;

public class PropertyEditorRegistry implements PropertyEditorFactory {
    private Map typeToEditor = new HashMap();
    private Map<AbstractPropertyEditor, Class> handleToEditor = new HashMap();
    private Map propertyToEditor = new HashMap();

    public PropertyEditorRegistry() {
        this.registerDefaults();
    }

    public PropertyEditor createPropertyEditor(Property property) {
        return this.getEditor(property);
    }

    public synchronized PropertyEditor getEditor(Property property) {
        PropertyEditor editor = null;
        Object value = this.propertyToEditor.get(property);
        if (value instanceof PropertyEditor) {
            editor = (PropertyEditor) value;
        } else if (value instanceof Class) {
            editor = this.loadPropertyEditor((Class) value);
        } else {
            editor = this.getEditor(property.getType());
        }

        if (editor != null && editor instanceof AbstractPropertyEditor) {
            ((AbstractPropertyEditor) editor).setProperty(property);
            ((AbstractPropertyEditor) editor).initEditor();
        }

        return editor;
    }

    private PropertyEditor loadPropertyEditor(Class clz) {
        PropertyEditor editor = null;

        try {
            editor = (PropertyEditor) clz.newInstance();
        } catch (Exception var4) {
            LogUtil.log.error("Error", var4);
        }

        return editor;
    }

    public synchronized PropertyEditor getEditor(Class type) {
        PropertyEditor editor = null;
        Object value = this.typeToEditor.get(type);
        if (value instanceof PropertyEditor) {
            editor = (PropertyEditor) value;
        } else if (value instanceof Class) {
            try {
                editor = (PropertyEditor) ((Class) value).newInstance();
            } catch (Exception var7) {
                LogUtil.log.error("Error", var7);
            }
        }

        if (editor == null) {
            Iterator i$ = this.handleToEditor.entrySet().iterator();

            label38:
            while (true) {
                Entry e;
                do {
                    if (!i$.hasNext()) {
                        break label38;
                    }

                    e = (Entry) i$.next();
                } while (!((AbstractPropertyEditor) e.getKey()).canHandle(type));

                try {
                    editor = (PropertyEditor) ((Class) e.getValue()).newInstance();
                    break;
                } catch (Exception var8) {
                    LogUtil.log.error("Error", var8);
                }
            }
        }

        if (editor instanceof AbstractPropertyEditor) {
            ((AbstractPropertyEditor) editor).setType(type);
        }

        return editor;
    }

    public synchronized void registerEditor(Class type, Class editorClass) {
        this.typeToEditor.put(type, editorClass);
    }

    public synchronized void registerEditor(Class type, PropertyEditor editor) {
        this.typeToEditor.put(type, editor);
    }

    public synchronized void unregisterEditor(Class type) {
        this.typeToEditor.remove(type);
    }

    public synchronized void registerEditor(Property property, Class editorClass) {
        this.propertyToEditor.put(property, editorClass);
    }

    public synchronized void registerEditor(Property property, PropertyEditor editor) {
        this.propertyToEditor.put(property, editor);
    }

    public synchronized void unregisterEditor(Property property) {
        this.propertyToEditor.remove(property);
    }

    public synchronized void registerEditor(AbstractPropertyEditor handler, Class editorClass) {
        this.handleToEditor.put(handler, editorClass);
    }

    public synchronized void unregisterEditor(AbstractPropertyEditor handler) {
        this.handleToEditor.remove(handler);
    }

    public void registerDefaults() {
        this.typeToEditor.clear();
        this.propertyToEditor.clear();
        this.registerEditor(String.class, StringPropertyEditor.class);
        this.registerEditor(TextAreaString.class, TextAreaPropertyEditor.class);
        this.registerEditor(PasswordString.class, PasswordPropertyEditor.class);
        this.registerEditor(ListString.class, ListPropertyEditor.class);
        this.registerEditor(Double.TYPE, DoublePropertyEditor.class);
        this.registerEditor(Double.class, DoublePropertyEditor.class);
        this.registerEditor(Float.TYPE, FloatPropertyEditor.class);
        this.registerEditor(Float.class, FloatPropertyEditor.class);
        this.registerEditor(Integer.TYPE, IntegerPropertyEditor.class);
        this.registerEditor(Integer.class, IntegerPropertyEditor.class);
        this.registerEditor(Long.TYPE, LongPropertyEditor.class);
        this.registerEditor(Long.class, LongPropertyEditor.class);
        this.registerEditor(Short.TYPE, ShortPropertyEditor.class);
        this.registerEditor(Short.class, ShortPropertyEditor.class);
        this.registerEditor(Boolean.TYPE, BooleanAsCheckBoxPropertyEditor.class);
        this.registerEditor(Boolean.class, BooleanAsCheckBoxPropertyEditor.class);
        this.registerEditor(File.class, FilePropertyEditor.class);
        this.registerEditor(Color.class, ColorPropertyEditor.class);
        this.registerEditor(Dimension.class, DimensionPropertyEditor.class);
        this.registerEditor(Insets.class, InsetsPropertyEditor.class);
        this.registerEditor(Rectangle.class, RectanglePropertyEditor.class);
        this.registerEditor(Vector.class, VectorTablePropertyEditor.class);
        this.registerEditor(Enum.class, EnumPropertyEditor.class);
        this.registerEditor(Map.class, MapPropertyEditor.class);
        this.registerEditor(ObjectHolder.class, DynamicPropertyEditor.class);

        try {
            Class.forName("com.toedter.calendar.JDateChooser");
            this.registerEditor(Date.class, Class.forName("com.l2fprod.common.beans.editor.JCalendarDatePropertyEditor"));
        } catch (ClassNotFoundException var2) {
        }

    }
}
