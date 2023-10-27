package com.telsoft.cbs.module.sms.handler;

import com.telsoft.dictionary.Dictionary;
import com.telsoft.dictionary.DictionaryNode;
import com.telsoft.util.StringUtil;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class CommandProcessor extends ChainCommandHandler implements ConfigurableCommandHandler, SessionAware {
    private List<SessionAware> objects = new ArrayList<>();

    @Override
    public void doProcess(CommandContext context) {

    }

    @Override
    public void configure(DictionaryNode config) throws Exception {
        String rootPackage = config.getStringIgnoreCase("Package");
        loadChild(rootPackage, this, config);
    }

    public void configure(String data) throws Exception {
        Dictionary dic = new Dictionary(new ByteArrayInputStream(data.getBytes()));
        configure(dic);
    }

    public void loadChild(String rootPackage, ChainCommandHandler chainCommandHandler, DictionaryNode root) throws Exception {
        Vector<DictionaryNode> children = root.getChildList();
        for (DictionaryNode child : children) {
            if (child.mstrName.equalsIgnoreCase("Handler")) {
                String classname = child.mstrValue;
                Class clazz;
                try {
                    clazz = Class.forName(rootPackage + "." + classname);
                } catch (ClassNotFoundException ex) {
                    clazz = Class.forName(classname);
                }
                CommandHandler handler = (CommandHandler) clazz.newInstance();
                if (handler instanceof ConfigurableCommandHandler) {
                    ((ConfigurableCommandHandler) handler).configure(child);
                }

                if (handler instanceof ChainCommandHandler) {
                    loadChild(rootPackage, (ChainCommandHandler) handler, child);
                }

                if (handler instanceof SessionAware) {
                    objects.add((SessionAware) handler);
                }

                List<CommandHandlerDefinition.KeyValue> keyValues = loadCondition(child);

                CommandHandlerDefinition definition = new CommandHandlerDefinition();
                definition.setKeyValues(keyValues);
                definition.setCommandHandler(handler);

                chainCommandHandler.addDefinition(definition);
            } else if (child.mstrName.equalsIgnoreCase("Otherwise")) {
                String classname = child.mstrValue;
                Class clazz;
                try {
                    clazz = Class.forName(rootPackage + "." + classname);
                } catch (ClassNotFoundException ex) {
                    clazz = Class.forName(classname);
                }
                CommandHandler handler = (CommandHandler) clazz.newInstance();
                if (handler instanceof ConfigurableCommandHandler) {
                    ((ConfigurableCommandHandler) handler).configure(child);
                }

                if (handler instanceof ChainCommandHandler) {
                    loadChild(rootPackage, (ChainCommandHandler) handler, child);
                }

                if (handler instanceof SessionAware) {
                    objects.add((SessionAware) handler);
                }
                chainCommandHandler.setOtherwise(handler);
            }
        }
    }

    private List<CommandHandlerDefinition.KeyValue> loadCondition(DictionaryNode root) {
        Vector<DictionaryNode> children = root.getChildList();
        for (DictionaryNode child : children) {
            if (child.mstrName.equalsIgnoreCase("Condition")) {
                List<CommandHandlerDefinition.KeyValue> ret = new ArrayList<>();
                Vector<DictionaryNode> cChildren = child.getChildList();
                for (DictionaryNode cChild : cChildren) {
                    String key = cChild.mstrName;
                    String values = cChild.mstrValue;
                    String[] arrayValues = StringUtil.toStringArray(values, ",");
                    for (String value : arrayValues) {
                        CommandHandlerDefinition.KeyValue kv = new CommandHandlerDefinition.KeyValue();
                        kv.setK(key);
                        kv.setV(value);
                        ret.add(kv);
                    }
                }
                return ret;
            }
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public void stop() {
        for (SessionAware aware : objects) {
            aware.stop();
        }

        objects.clear();
        objects = null;
    }

    @Override
    public void start() throws Exception {
        for (SessionAware aware : objects) {
            aware.start();
        }
    }
}
