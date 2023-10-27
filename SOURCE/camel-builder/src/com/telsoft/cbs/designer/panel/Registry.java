package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import org.apache.camel.NamedNode;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Registry {
    private static final Map<Class, NodeHelper> registry = new HashMap<>();
    private static AtomicBoolean hasInit = new AtomicBoolean(false);
    private static Lock lock = new ReentrantLock();

    private static List<Class<? extends NodeHelper>> enumerateHelpers() {
        Reflections reflections = new Reflections(new SubTypesScanner(true));
        Set<Class<? extends NodeHelper>> responses = reflections.getSubTypesOf(NodeHelper.class);
        List<Class<? extends NodeHelper>> result = new ArrayList<>();
        for (Class clazz : responses) {
            if (Modifier.isAbstract(clazz.getModifiers()))
                continue;

            if (Modifier.isInterface(clazz.getModifiers()))
                continue;

            String clazzName = clazz.getName();

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
        return result;
    }

    private static void scan() {
        lock.lock();
        try {
            if (hasInit.get())
                return;
            List<Class<? extends NodeHelper>> helpers = enumerateHelpers();
            for (Class<? extends NodeHelper> clazz : helpers) {
                try {
                    NodeHelper helper = clazz.newInstance();
                    Class nodeClazz = helper.getClazz();
                    registry.put(nodeClazz, helper);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }
            hasInit.set(true);
        } finally {
            lock.unlock();
        }
    }

    public static <T extends NamedNode> NodeHelper<T> getHelper(T object) {
        if (object == null)
            return null;

        if (!hasInit.get()) {
            scan();
        }

        return registry.get(object.getClass());
    }

    public static <T extends NamedNode> NodeHelper<T> getHelper(Class<T> clazz) {
        if (clazz == null)
            return null;

        if (!hasInit.get()) {
            scan();
        }

        return registry.get(clazz);
    }

    public static String getGroup(Class clazz) {
        if (clazz == null)
            return null;

        if (!hasInit.get()) {
            scan();
        }

        NodeHelper helper = registry.get(clazz);
        if (helper == null)
            return null;

        return helper.getGroup();
    }
}
