package com.telsoft.cbs.designer.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.LoadPropertiesException;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.engine.DefaultInjector;
import org.apache.camel.runtimecatalog.RuntimeCamelCatalog;
import org.apache.camel.runtimecatalog.impl.DefaultRuntimeCamelCatalog;
import org.apache.camel.spi.ClassResolver;
import org.apache.camel.util.IOHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;

public final class CamelHelper {
    public static final String COMPONENT_BASE = "META-INF/services/org/apache/camel/component/";
    public static final String COMPONENT_DESCRIPTOR = "META-INF/services/org/apache/camel/component.properties";
    public static final String LANGUAGE_DESCRIPTOR = "META-INF/services/org/apache/camel/language.properties";
    public static final String LANGUAGE_BASE = "META-INF/services/org/apache/camel/language/";

    private static final Logger LOG = LoggerFactory.getLogger(CamelHelper.class);

    private CamelHelper() {
    }

    public static DefaultCamelContext createCamelContext() {
        DefaultCamelContext context = new DefaultCamelContext();
        context.setInjector(new DefaultInjector(context));
        context.setAutoStartup(true);
        context.setMessageHistory(true);
        context.disableJMX();
        return context;
    }

    public static SortedMap<String, Class> findLanguages(CamelContext context) throws LoadPropertiesException {
        ClassResolver resolver = context.getClassResolver();
        Enumeration<URL> iter = resolver.loadAllResourcesAsURL(LANGUAGE_DESCRIPTOR);
        return findLanguages(context, iter);
    }

    public static Set<Class> findListLanguage(CamelContext context) throws LoadPropertiesException {
        Map<String, Class> map = findLanguages(context);
        LinkedHashSet set = new LinkedHashSet();
        for (Class clazz : map.values()) {
            set.add(clazz);
        }
        return set;
    }

    public static SortedMap<String, Class> findLanguages(CamelContext camelContext, Enumeration<URL> componentDescriptionIter)
            throws LoadPropertiesException {

        SortedMap<String, Class> map = new TreeMap<>();
        while (componentDescriptionIter != null && componentDescriptionIter.hasMoreElements()) {
            URL url = componentDescriptionIter.nextElement();
            try {
                Properties properties = new Properties();
                properties.load(url.openStream());
                String names = properties.getProperty("languages");
                if (names != null) {
                    StringTokenizer tok = new StringTokenizer(names);
                    while (tok.hasMoreTokens()) {
                        String name = tok.nextToken();

                        RuntimeCamelCatalog catalog = new DefaultRuntimeCamelCatalog(camelContext, true);
                        String s = catalog.languageJSonSchema(name);
                        if (s != null && s.length() > 0) {
                            ObjectMapper mapper = new ObjectMapper();
                            try {
                                Map info = mapper.readValue(s, Map.class);
                                Map<String, String> language = (Map<String, String>) info.get("language");
                                String clazz = language.get("modelJavaType");
                                map.put(name, Class.forName(clazz));
                            } catch (Exception ignored) {
                                ignored.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                throw new LoadPropertiesException(url, e);
            }
        }
        return map;
    }

    public static List<String> findComponents(CamelContext camelContext) throws LoadPropertiesException {
        ClassResolver resolver = camelContext.getClassResolver();
        LOG.debug("Finding all components using class resolver: {} -> {}", resolver);
        Enumeration<URL> iter = resolver.loadAllResourcesAsURL(COMPONENT_DESCRIPTOR);
        return findComponents(camelContext, iter);
    }

    public static List<String> findComponents(CamelContext camelContext, Enumeration<URL> componentDescriptionIter) throws LoadPropertiesException {
        List<String> map = new ArrayList<>();

        while (componentDescriptionIter != null && componentDescriptionIter.hasMoreElements()) {
            URL url = (URL) componentDescriptionIter.nextElement();
            LOG.trace("Finding components in url: {}", url);

            try {
                Properties properties = new Properties();
                properties.load(url.openStream());
                String name = properties.getProperty("components");
                if (name != null) {
                    StringTokenizer tok = new StringTokenizer(name);

                    while (tok.hasMoreTokens()) {
                        name = tok.nextToken();
                        String className = null;
                        InputStream is = null;

                        try {
                            Enumeration<URL> urls = camelContext.getClassResolver().loadAllResourcesAsURL(COMPONENT_BASE + name);
                            if (urls != null && urls.hasMoreElements()) {
                                is = ((URL) urls.nextElement()).openStream();
                            }

                            if (is != null) {
                                Properties compProperties = new Properties();
                                compProperties.load(is);
                                if (!compProperties.isEmpty()) {
                                    className = compProperties.getProperty("class");
                                }
                            }
                        } catch (Exception var16) {
                        } finally {
                            IOHelper.close(is);
                        }
                        map.add(name);
                    }
                }
            } catch (IOException var18) {
                throw new LoadPropertiesException(url, var18);
            }
        }

        List<String> names = camelContext.getComponentNames();
        Iterator var20 = names.iterator();

        while (var20.hasNext()) {
            String name = (String) var20.next();
            if (!map.contains(name)) {
                map.add(name);
            }
        }

        Map<String, Component> beanMap = camelContext.getRegistry().findByTypeWithName(Component.class);
        Set<Entry<String, Component>> entries = beanMap.entrySet();
        Iterator var24 = entries.iterator();

        while (var24.hasNext()) {
            Entry<String, Component> entry = (Entry) var24.next();
            String className = (String) entry.getKey();
            if (!map.contains(className)) {
                Component component = (Component) entry.getValue();
                if (component != null) {
                    map.add(className);
                }
            }
        }
        return map;
    }
}
