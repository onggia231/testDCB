package com.telsoft.cbs.designer.dialog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.telsoft.cbs.designer.property.CamelFactory;
import com.telsoft.cbs.designer.utils.CamelHelper;
import com.telsoft.cbs.designer.utils.ComponentInfo;
import com.telsoft.cbs.designer.utils.ParameterInfo;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.runtimecatalog.RuntimeCamelCatalog;
import org.apache.camel.runtimecatalog.impl.DefaultRuntimeCamelCatalog;

import javax.swing.*;
import java.awt.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentSelectorDialog extends BaseDialog {
    private final boolean input;
    private String uri;
    private Map<String, ParameterInfo> parameters = new HashMap<>();

    private JList<ComponentInfo> list = new JList<>(new DefaultListModel<>());

    public ComponentSelectorDialog(Frame parent, boolean modal, boolean input) {
        super(parent, modal);
        this.input = input;
    }

    public String getUri() {
        StringBuffer buf = new StringBuffer();
        buf.append(uri).append(":");

        int i = 0;
        for (ParameterInfo pi : parameters.values()) {
            if (pi.getType() == ParameterInfo.TYPE.path && (pi.getValue() != null && String.valueOf(pi.getValue()).length() > 0)) {
                i++;
                buf.append(pi.getValue());
            }
        }

        if (parameters.size() > i) {
            buf.append("?");
            for (ParameterInfo pi : parameters.values()) {
                if (pi.getType() == ParameterInfo.TYPE.parameter) {
                    if (pi.getValue() != null && String.valueOf(pi.getValue()).length() > 0) {
                        buf
                                .append(pi.getName())
                                .append("=")
                                .append(pi.getValue())
                                .append("&");
                    }
                }
            }
            if (buf.charAt(buf.length() - 1) == '&' || buf.charAt(buf.length() - 1) == '?') {
                buf.deleteCharAt(buf.length() - 1);
            }
        }
        if (buf.charAt(buf.length() - 1) == ':') {
            buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString();
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    protected String getTitleText() {
        return input ? "Select consumer" : "Select producer";
    }

    @Override
    protected void initContent(JPanel panel) {
        JSplitPane panelContent = new JSplitPane();
        panelContent.setDividerLocation(300);
        panel.setLayout(new GridBagLayout());
        panel.add(panelContent,new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2));

        JPanel panelBean = new JPanel();
        panelBean.setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(list);
        panelBean.add(scrollPane, BorderLayout.CENTER);

//        panelContent.add(panelBean, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2));
        panelContent.setLeftComponent(panelBean);
        PropertySheetPanel properties = new PropertySheetPanel();
        properties.setMode(PropertySheet.VIEW_AS_CATEGORIES);
        properties.setDescriptionVisible(true);
        CamelFactory.registerTo(properties);
        properties.addPropertySheetChangeListener(evt -> {
            DefaultProperty p = (DefaultProperty) evt.getSource();
            ParameterInfo pi = parameters.get(p.getName());
            pi.setValue(p.getValue());
        });

//        panelContent.add(properties, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2));
        panelContent.setRightComponent(properties);
        list.setCellRenderer(new ComponentCellRenderer());
        list.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting())
                return;

            ComponentInfo ci = list.getSelectedValue();
            if (ci != null) {
                Property[] pp = ci.getPropertyList(parameters);
                if (pp != null)
                    properties.setProperties(pp);
                else
                    properties.setProperties(new Property[0]);
            }
        });

        DefaultCamelContext context = CamelHelper.createCamelContext();
        try {
            List<String> comps = CamelHelper.findComponents(context);
            comps.sort((o1, o2) -> o1.compareTo(o2));

            String componentName = null;
            ComponentInfo selected = null;
            if (uri != null && uri.length() > 0) {
                try {
                    URI uri_ = new URI(uri);
                    componentName = uri_.getScheme();
                    if (componentName == null)
                        componentName = uri_.getSchemeSpecificPart();
                } catch (Exception ex) {
                    int index = uri.indexOf(":");
                    if (index >= 0) {
                        componentName = uri.substring(0, index);
                    } else {
                        componentName = uri;
                    }
                }
            }

            RuntimeCamelCatalog catalog = new DefaultRuntimeCamelCatalog(context, true);
            for (String name : comps) {
                ComponentInfo ci = new ComponentInfo();
                ci.setName(name);

                String s = catalog.componentJSonSchema(ci.getName());
                if (s != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        ci.setSchema(mapper.readValue(s, Map.class));
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }

                if (componentName != null && componentName.equals(ci.getName()))
                    parameters = ci.extractUrl(uri);

                if ((input && ci.isConsumer()) || (!input && ci.isProducer())) {
                    ((DefaultListModel) list.getModel()).addElement(ci);
                    if (componentName != null && componentName.equals(name)) {
                        selected = ci;
                    }
                }
            }

            if (selected != null) {
                list.setSelectedValue(selected, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onOK() {
        ComponentInfo ci = list.getSelectedValue();
        uri = ci.getName();
        super.onOK();
    }
}
