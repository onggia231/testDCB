//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.telsoft.cbs.loader;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.camel.*;
import org.apache.camel.converter.jaxp.XmlConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ExpressionNode;
import org.apache.camel.model.ProcessorDefinitionHelper;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.apache.camel.model.language.ExpressionDefinition;
import org.apache.camel.spi.NamespaceAware;
import org.apache.camel.spi.TypeConverterRegistry;
import org.apache.camel.util.ObjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.bind.Binder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

public final class ModelHelper {

    private static JAXBContext cachedInstance = null;

    private ModelHelper() {
    }

    public static String saveBean(Beans beans) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        mapper.writeValue(bos, beans);
        return new String(bos.toByteArray());
    }

    public static Beans loadBean(InputStream in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(in, Beans.class);
    }

    public static String dumpModelAsXml(DefaultCamelContext context, NamedNode definition) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(context);
        Map<String, String> namespaces = new LinkedHashMap();
        if (definition instanceof RoutesDefinition) {
            List<RouteDefinition> routes = ((RoutesDefinition) definition).getRoutes();
            Iterator var5 = routes.iterator();

            while (var5.hasNext()) {
                RouteDefinition route = (RouteDefinition) var5.next();
                extractNamespaces((RouteDefinition) route, namespaces);
            }
        } else if (definition instanceof RouteDefinition) {
            RouteDefinition route = (RouteDefinition) definition;
            extractNamespaces((RouteDefinition) route, namespaces);
        }

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty("jaxb.encoding", "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        StringWriter buffer = new StringWriter();
        marshaller.marshal(definition, buffer);
        XmlConverter xmlConverter = newXmlConverter(context);
        String xml = buffer.toString();

        Document dom;
        try {
            dom = xmlConverter.toDOMDocument(xml, (Exchange) null);
        } catch (Exception var14) {
            throw new TypeConversionException(xml, Document.class, var14);
        }

        Element documentElement = dom.getDocumentElement();
        Iterator var10 = namespaces.keySet().iterator();

        while (var10.hasNext()) {
            String nsPrefix = (String) var10.next();
            String prefix = nsPrefix.equals("xmlns") ? nsPrefix : "xmlns:" + nsPrefix;
            documentElement.setAttribute(prefix, (String) namespaces.get(nsPrefix));
        }

        Properties outputProperties = new Properties();
        outputProperties.put("indent", "yes");
        outputProperties.put("standalone", "yes");
        outputProperties.put("encoding", "UTF-8");

        try {
            return xmlConverter.toStringFromDocument(dom, outputProperties);
        } catch (TransformerException var13) {
            throw new IllegalStateException("Failed converting document object to string", var13);
        }
    }

    public static <T extends NamedNode> T createModelFromXml(DefaultCamelContext context, String xml, Class<T> type) throws JAXBException {
        return modelToXml(context, (InputStream) null, xml, type);
    }

    public static <T extends NamedNode> T createModelFromXml(DefaultCamelContext context, InputStream stream, Class<T> type) throws JAXBException {
        return modelToXml(context, stream, (String) null, type);
    }

    public static RoutesDefinition loadRoutesDefinition(DefaultCamelContext context, InputStream inputStream) throws Exception {
        XmlConverter xmlConverter = newXmlConverter(context);
        Document dom = xmlConverter.toDOMDocument(inputStream, (Exchange) null);
        return loadRoutesDefinition(context, (Node) dom);
    }

    public static RoutesDefinition loadRoutesDefinition(DefaultCamelContext context, Node node) throws Exception {
        JAXBContext jaxbContext = getJAXBContext(context);
        Map<String, String> namespaces = new LinkedHashMap();
        Document dom = node instanceof Document ? (Document) node : node.getOwnerDocument();
        extractNamespaces((Document) dom, namespaces);
        Binder<Node> binder = jaxbContext.createBinder();
        Object result = binder.unmarshal(node);
        if (result == null) {
            throw new JAXBException("Cannot unmarshal to RoutesDefinition using JAXB");
        } else {
            RoutesDefinition answer;
            if (result instanceof RouteDefinition) {
                RouteDefinition route = (RouteDefinition) result;
                answer = new RoutesDefinition();
                applyNamespaces(route, namespaces);
                answer.getRoutes().add(route);
            } else {
                if (!(result instanceof RoutesDefinition)) {
                    throw new IllegalArgumentException("Unmarshalled object is an unsupported type: " + ObjectHelper.className(result) + " -> " + result);
                }

                answer = (RoutesDefinition) result;
                Iterator var10 = answer.getRoutes().iterator();

                while (var10.hasNext()) {
                    RouteDefinition route = (RouteDefinition) var10.next();
                    applyNamespaces(route, namespaces);
                }
            }

            return answer;
        }
    }

    private static <T extends NamedNode> T modelToXml(DefaultCamelContext context, InputStream is, String xml, Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = getJAXBContext(context);
        XmlConverter xmlConverter = newXmlConverter(context);
        Document dom = null;

        try {
            if (is != null) {
                dom = xmlConverter.toDOMDocument(is, (Exchange) null);
            } else if (xml != null) {
                dom = xmlConverter.toDOMDocument(xml, (Exchange) null);
            }
        } catch (Exception var13) {
            throw new TypeConversionException(xml, Document.class, var13);
        }

        if (dom == null) {
            throw new IllegalArgumentException("InputStream and XML is both null");
        } else {
            Map<String, String> namespaces = new LinkedHashMap();
            extractNamespaces((Document) dom, namespaces);
            Binder<Node> binder = jaxbContext.createBinder();
            Object result = binder.unmarshal(dom);
            if (result == null) {
                throw new JAXBException("Cannot unmarshal to " + type + " using JAXB");
            } else {
                if (result instanceof RoutesDefinition) {
                    List<RouteDefinition> routes = ((RoutesDefinition) result).getRoutes();
                    Iterator var11 = routes.iterator();

                    while (var11.hasNext()) {
                        RouteDefinition route = (RouteDefinition) var11.next();
                        applyNamespaces(route, namespaces);
                    }
                } else if (result instanceof RouteDefinition) {
                    RouteDefinition route = (RouteDefinition) result;
                    applyNamespaces(route, namespaces);
                }

                return (T) type.cast(result);
            }
        }
    }

    private static JAXBContext getJAXBContext(DefaultCamelContext context) throws JAXBException {
        JAXBContext jaxbContext;
        if (context == null) {
            jaxbContext = createJAXBContext();
        } else {
            jaxbContext = context.getModelJAXBContextFactory().newJAXBContext();
        }

        return jaxbContext;
    }

    private static void applyNamespaces(RouteDefinition route, Map<String, String> namespaces) {
        Iterator it = ProcessorDefinitionHelper.filterTypeInOutputs(route.getOutputs(), ExpressionNode.class);

        while (it.hasNext()) {
            NamespaceAware na = getNamespaceAwareFromExpression((ExpressionNode) it.next());
            if (na != null) {
                na.setNamespaces(namespaces);
            }
        }

    }

    private static NamespaceAware getNamespaceAwareFromExpression(ExpressionNode expressionNode) {
        ExpressionDefinition ed = expressionNode.getExpression();
        if (ed == null)
            return null;
        NamespaceAware na = null;
        Expression exp = ed.getExpressionValue();
        if (exp instanceof NamespaceAware) {
            na = (NamespaceAware) exp;
        } else if (ed instanceof NamespaceAware) {
            na = (NamespaceAware) ed;
        }

        return na;
    }

    private static JAXBContext createJAXBContext() throws JAXBException {
        if (cachedInstance == null)
            cachedInstance = JAXBContext.newInstance(
                    "org.apache.camel" +
                            ":org.apache.camel.model" +
                            ":org.apache.camel.model.cloud" +
                            ":org.apache.camel.model.config" +
                            ":org.apache.camel.model.dataformat" +
                            ":org.apache.camel.model.language" +
                            ":org.apache.camel.model.loadbalancer" +
                            ":org.apache.camel.model.rest" +
                            ":org.apache.camel.model.transformer" +
                            ":org.apache.camel.model.validator", CamelContext.class.getClassLoader());
        return cachedInstance;
    }

    private static void extractNamespaces(RouteDefinition route, Map<String, String> namespaces) {
        Iterator it = ProcessorDefinitionHelper.filterTypeInOutputs(route.getOutputs(), ExpressionNode.class);

        while (it.hasNext()) {
            NamespaceAware na = getNamespaceAwareFromExpression((ExpressionNode) it.next());
            if (na != null) {
                Map<String, String> map = na.getNamespaces();
                if (map != null && !map.isEmpty()) {
                    namespaces.putAll(map);
                }
            }
        }

    }

    private static void extractNamespaces(Document document, Map<String, String> namespaces) throws JAXBException {
        NamedNodeMap attributes = document.getDocumentElement().getAttributes();

        for (int i = 0; i < attributes.getLength(); ++i) {
            Node item = attributes.item(i);
            String nsPrefix = item.getNodeName();
            if (nsPrefix != null && nsPrefix.startsWith("xmlns")) {
                String nsValue = item.getNodeValue();
                String[] nsParts = nsPrefix.split(":");
                if (nsParts.length == 1) {
                    namespaces.put(nsParts[0], nsValue);
                } else if (nsParts.length == 2) {
                    namespaces.put(nsParts[1], nsValue);
                } else {
                    namespaces.put(nsPrefix, nsValue);
                }
            }
        }

    }

    private static XmlConverter newXmlConverter(CamelContext context) {
        XmlConverter xmlConverter;
        if (context != null) {
            TypeConverterRegistry registry = context.getTypeConverterRegistry();
            xmlConverter = (XmlConverter) registry.getInjector().newInstance(XmlConverter.class);
        } else {
            xmlConverter = new XmlConverter();
        }

        return xmlConverter;
    }
}
