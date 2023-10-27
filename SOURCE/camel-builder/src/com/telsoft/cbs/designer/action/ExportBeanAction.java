package com.telsoft.cbs.designer.action;

import com.l2fprod.common.swing.JDirectoryChooser;
import com.squareup.javapoet.*;
import com.telsoft.cbs.designer.panel.BeanManager;
import com.telsoft.cbs.designer.panel.FormMain;
import com.telsoft.cbs.loader.BeanInfo;
import com.telsoft.swing.MessageBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.Modifier;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Service
public class ExportBeanAction extends CBAction {
    @Autowired
    private BeanManager beanManager;

    private FormMain formMain;

    public ExportBeanAction(@Autowired FormMain formMain) {
        super("Export Beans");
        setMnemonic(KeyEvent.VK_E);
    }

    public static void exportToJava(BeanManager beanManager, File file) throws IOException {
        // TYPES
        ClassName object = ClassName.get("java.lang", "Object");
        ClassName string = ClassName.get("java.lang", "String");
        ClassName map = ClassName.get("java.util", "Map");
        ClassName mapEntry = ClassName.get("java.util", "Map.Entry");
        ClassName hashmap = ClassName.get("java.util", "HashMap");

        ClassName registry = ClassName.get("org.apache.camel.spi", "Registry");
        ClassName simpleregistry = ClassName.get("org.apache.camel.support", "SimpleRegistry");
        ClassName context = ClassName.get("org.apache.camel", "CamelContext");
        ClassName contextAware = ClassName.get("org.apache.camel", "CamelContextAware");
        ClassName endpoint = ClassName.get("org.apache.camel", "Endpoint");
        ClassName objenesis = ClassName.get("org.objenesis", "Objenesis");
        ClassName objenesisstd = ClassName.get("org.objenesis", "ObjenesisStd");

        ClassName exception = ClassName.get("java.lang", "Exception");
        ClassName clazz = ClassName.get("java.lang", "Class");

        // PARAMETERIZED TYPES
        TypeVariableName type = TypeVariableName.get("<T> T");
        TypeName mapStringObject = ParameterizedTypeName.get(map, string, object);
        TypeName mapEntryStringObject = ParameterizedTypeName.get(mapEntry, string, object);
        TypeName haspMapStringObject = ParameterizedTypeName.get(hashmap, string, object);


        // FIELDS
        FieldSpec beans = FieldSpec.builder(mapStringObject, "beans", Modifier.PRIVATE)
                .initializer("new $T()", haspMapStringObject)
                .build();

        // INIT BLOCK CODE
        CodeBlock.Builder initBeans = CodeBlock.builder();
        for (Map.Entry<String, BeanInfo> e : beanManager.getBeans().entrySet()) {
            String id = e.getKey();
            Object value;
            String className;
            try {
                value = e.getValue().getBeanObject();
                className = value.getClass().getName();
            } catch (Exception e1) {
                className = e.getValue().getClazz();
                if (className == null || className.length() == 0)
                    continue;
            }

            initBeans.add("// $N\r\n", id);
            try {
                Class clzz = Class.forName(className);
                clzz.newInstance();
                ClassName clzzName = ClassName.get(clzz);
                initBeans.addStatement("$T $N = new $T()", clzzName, id, clzzName);
            } catch (Exception ex) {
                initBeans.addStatement("Class $N_class = Class.forName($N)", id, className);
                initBeans.addStatement("$N $N = create($N_class)", className, id, id);
            }
            initBeans.addStatement("beans.put($S,$N)", id, id);
            initBeans.add("\r\n");
        }


        // METHODS
        MethodSpec createRegistrySpec = MethodSpec.methodBuilder("createRegistry")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addException(exception)
                .returns(registry)
                .addParameter(context, "context")
                .addStatement("$T registry = new $T()", registry, simpleregistry)
                .beginControlFlow("for ($T e : getBeans().entrySet())", mapEntryStringObject)
                .addStatement("$T o = e.getValue()", object)
                .beginControlFlow("if (o instanceof $T)", contextAware)
                .addStatement("(($T)o).setCamelContext($N)", contextAware, "context")
                .nextControlFlow("else if (o instanceof $T)", endpoint)
                .addStatement("(($T)o).setCamelContext($N)", endpoint, "context")
                .endControlFlow()
                .addStatement("registry.bind(e.getKey(), o)")
                .endControlFlow()
                .addStatement("return registry")
                .build();

        MethodSpec getBeansSpec = MethodSpec.methodBuilder("getBeans")
                .returns(mapStringObject)
                .addStatement("return beans")
                .addModifiers(Modifier.PUBLIC)
                .build();

        MethodSpec createSpec = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(type)
                .addParameter(clazz, "clazz")
                .addStatement("$T objenesis = new $T()", objenesis, objenesisstd)
                .addStatement("return (T) objenesis.newInstance($N)", "clazz")
                .build();

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addException(exception)
                .addCode(initBeans.build())
                .build();


        // CLASSES
        TypeSpec bmSpec = TypeSpec.classBuilder("BeanManager")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addField(beans)
                .addMethod(constructor)
                .addMethod(createRegistrySpec)
                .addMethod(createSpec)
                .addMethod(getBeansSpec)
                .build();

        // WRITE
        JavaFile javaFile = JavaFile.builder("<change.me>", bmSpec)
                .skipJavaLangImports(true)
                .addFileComment("This file is generated by Telsoft Camel Builder\r\n")
                .addFileComment("DO NOT MODIFY")
                .build();
        try (BufferedWriter bout = new BufferedWriter(new FileWriter(file))) {
            javaFile.writeTo(bout);
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        export();
    }

    private void export() {
        JDirectoryChooser saveDialog = new JDirectoryChooser("./");
        saveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        int result = saveDialog.showSaveDialog(formMain);
        if (result == JFileChooser.APPROVE_OPTION) {
            File directory = saveDialog.getSelectedFile();
            try {
                File file = new File(directory.getAbsolutePath() + File.separator + "BeanManager.java");
                exportToJava(beanManager, file);
                MessageBox.showMessageDialog("Beans are exported to file " + file.getAbsoluteFile());
            } catch (IOException e) {
                MessageBox.showMessageDialog(formMain, e);
            }
        }
    }
}
