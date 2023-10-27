package com.telsoft.cbs.designer;

import com.telsoft.cbs.designer.action.ActionList;
import com.telsoft.cbs.designer.panel.FormMain;
import com.telsoft.util.Global;
import lombok.Getter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class App {
    @Getter
    private static AnnotationConfigApplicationContext context;

    public static void main(String[] args) throws Exception {
        BasicConfigurator.configure();
        Logger.getRootLogger().setLevel(Level.ERROR);

        Global.APP_NAME = "Telsoft Camel Builder";
        Global.APP_VERSION = "1.0";
        try {
            com.telsoft.dictionary.ErrorDictionary.appendDictionary("resource/com/telsoft/dictionary/Dictionary.txt", true);
            com.telsoft.dictionary.ErrorDictionary.setCurrentLanguage("EN");
            com.telsoft.dictionary.DefaultDictionary.setCurrentLanguage("EN");
        } catch (Exception e) {
            e.printStackTrace();
        }

        context = new AnnotationConfigApplicationContext();
        context.scan(FormMain.class.getPackage().getName(), ActionList.class.getPackage().getName());
        context.refresh();

        FormMain formMain = context.getBean(FormMain.class);
        formMain.formInit();
        formMain.run(args);

//  Hawtio
//        System.setProperty("hawtio.authenticationEnabled", "false");
//
//        Main main = new Main();
//        main.setWar("/data/local/download/hawtio.war");
//        main.run();
    }

    public FormMain getFormMain() {
        return context.getBean(FormMain.class);
    }

}
