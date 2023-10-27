package com.telsoft.cbs.designer.property;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.renderer.DefaultCellRenderer;
import com.l2fprod.common.propertysheet.renderer.PropertyCellRenderer;
import com.telsoft.cbs.designer.property.editor.*;
import com.telsoft.cbs.designer.property.renderer.DescriptionDefinitionRenderer;
import com.telsoft.cbs.designer.property.renderer.ListOfExceptionRenderer;
import com.telsoft.cbs.designer.property.renderer.ObjectRenderer;
import com.telsoft.cbs.designer.property.type.*;
import lombok.Getter;
import org.apache.camel.Processor;
import org.apache.camel.model.*;
import org.apache.camel.model.language.ExpressionDefinition;

import java.beans.PropertyEditor;

//import org.apache.camel.component.hazelcast.HazelcastOperation;

public class CamelFactory {
    @Getter
    private PropertyEditorRegistry editorFactory = new PropertyEditorRegistry();

    @Getter
    private PropertyRendererRegistry rendererFactory = new PropertyRendererRegistry();

    public CamelFactory() {
        registerDefaults();
    }

    public static Class getType(Object node, String name, String javaType, DefaultProperty p) {
        try {
            if (node instanceof ProcessDefinition && name.equals("ref")) {
                p.setUserdata("baseClass", Processor.class);
                return BeanString.class;
            }

            if (node instanceof FromDefinition && name.equals("ref")) {
                return Removed.class;
            }

            if (node instanceof RouteDefinition && name.equals("input")) {
                return Removed.class;
            }

            if (node instanceof RoutesDefinition && name.equals("routes")) {
                return Removed.class;
            }

            if (node instanceof ProcessorDefinition && name.equals("outputs")) {
                return Removed.class;
            }

            if (node instanceof ToDefinition && name.equals("ref")) {
                return Removed.class;
            }

            if (node instanceof ToDynamicDefinition && name.equals("ref")) {
                return Removed.class;
            }

            if (node instanceof FromDefinition && name.equals("uri")) {
                p.setUserdata("input", "1");
                return From.class;
            }

            if (node instanceof ToDefinition && name.equals("uri")) {
                p.setUserdata("input", "0");
                return To.class;
            }

            if (node instanceof ToDynamicDefinition && name.equals("uri")) {
                p.setUserdata("input", "0");
                return To.class;
            }

            if (node instanceof OnExceptionDefinition && name.equals("exception"))
                return ListOfException.class;

            if (node instanceof CatchDefinition && name.equals("exception"))
                return ListOfException.class;

            if (node instanceof ThrowExceptionDefinition && name.equals("exceptionType"))
                return OneException.class;

            return Class.forName(javaType);
        } catch (ClassNotFoundException e) {
            return Void.TYPE;
        }
    }

    public static void registerTo(PropertySheetPanel sheetPanel) {
        CamelFactory factory = new CamelFactory();
        sheetPanel.setEditorFactory(factory.getEditorFactory());
        sheetPanel.setRendererFactory(factory.getRendererFactory());
    }

    private void register(Class type, Class<? extends PropertyEditor> editorClass, Class<? extends PropertyCellRenderer> rendererClass) {
        editorFactory.registerEditor(type, editorClass);
        rendererFactory.registerRenderer(type, rendererClass);
    }

    private void registerDefaults() {
        register(String.class, MyStringEditor.class, DefaultCellRenderer.class);

        register(DescriptionDefinition.class, DescriptionDefinitionEditor.class, DescriptionDefinitionRenderer.class);
        register(BeanString.class, BeanStringEditor.class, ObjectRenderer.class);
        register(ListOfException.class, ListOfExceptionEditor.class, ListOfExceptionRenderer.class);

        register(From.class, EndpointEditor.class, ObjectRenderer.class);
        register(To.class, EndpointEditor.class, ObjectRenderer.class);

        register(OneException.class, ExceptionEditor.class, ObjectRenderer.class);
        register(OneClass.class, ClassEditor.class, ObjectRenderer.class);
        register(ExpressionDefinition.class, ExpressionEditor.class, ObjectRenderer.class);
        register(HystrixConfigurationDefinition.class, HystrixConfigurationEditor.class, ObjectRenderer.class);
        register(Resilience4jConfigurationDefinition.class, Resilience4jConfigurationEditor.class, ObjectRenderer.class);
        register(Bean.class, BeanPropertyEditor.class, ObjectRenderer.class);
//        register(HazelcastOperation.class, HazelcastOperatorPropertyEditor.class, DefaultCellRenderer.class);
        register(Enum.class, EnumPropertyEditor.class, DefaultCellRenderer.class);

        editorFactory.registerEditor(new com.l2fprod.common.propertysheet.editor.EnumPropertyEditor(), EnumPropertyEditor.class);

    }
}
