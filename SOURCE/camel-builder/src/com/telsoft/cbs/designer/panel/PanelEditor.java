package com.telsoft.cbs.designer.panel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.l2fprod.common.propertysheet.BeanUtils;
import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.telsoft.cbs.designer.property.CamelFactory;
import com.telsoft.cbs.designer.property.type.BeanString;
import com.telsoft.cbs.designer.property.type.RealType;
import com.telsoft.cbs.designer.property.type.Removed;
import com.telsoft.cbs.designer.utils.CamelHelper;
import com.telsoft.cbs.designer.utils.TreeUtils;
import com.telsoft.swing.JXPanel;
import com.telsoft.swing.JXText;
import com.telsoft.swing.JXTextArea;
import com.telsoft.swing.MessageBox;
import org.apache.camel.NamedNode;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.runtimecatalog.RuntimeCamelCatalog;
import org.apache.camel.runtimecatalog.impl.DefaultRuntimeCamelCatalog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
@Service
public class PanelEditor extends CBPanel {
    @Autowired
    private FormMain formMain;

    @Autowired
    private PanelDesign panelDesign;

    @Autowired
    private BeanManager beanManager;

    @Autowired
    private FileEditor fileEditor;

    private JXText title = new JXText();
    private JXTextArea description = new JXTextArea();
    private JScrollPane spDescription = new JScrollPane(description);
    private JXPanel panelDescription = new JXPanel();
    private PropertySheetPanel properties = new PropertySheetPanel();

    private AtomicBoolean restoring = new AtomicBoolean(false);

    private TreePath current;

    public PanelEditor() {
        super();
        setLayout(new BorderLayout());

        add(panelDescription, BorderLayout.NORTH);
        panelDescription.setLayout(new GridBagLayout());
        GridBagConstraints gcbLabel = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2);
        GridBagConstraints gcbControl = new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2);
        GridBagConstraints gcbControlFull = new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2);


        panelDescription.add(new Label("Title"), gcbLabel);
        panelDescription.add(title, gcbControl);
        title.setEditable(false);

        gcbLabel.gridy++;
        gcbControlFull.gridy++;
        panelDescription.add(new Label("Description"), gcbLabel);
        panelDescription.add(spDescription, gcbControlFull);
        description.setEditable(false);
        description.setLineWrap(true);
        description.setRows(2);
        description.setBackground(description.getParent().getBackground());

        add(properties, BorderLayout.CENTER);

        CamelFactory.registerTo(properties);
        properties.setDescriptionVisible(true);
        properties.setSorting(true);
        properties.setEditable(true);
        properties.addPropertySheetChangeListener(evt -> {
            if (restoring.get())
                return;

            DefaultProperty p = (DefaultProperty) evt.getSource();
            if (current != null) {
                NamedNode obj = TreeUtils.getCurrentNode(current);
                writeToObject(p, obj, evt.getOldValue());
                PanelDesign.CBTreeModel model = panelDesign.getTree().getModel();
                model.nodeChanged(current);
                fileEditor.setModified();
            }
        });
    }

    public void writeToObject(DefaultProperty p, Object object, Object oldValue) {
        try {
            Class type = p.getType();

            RealType realType = (RealType) type.getAnnotation(RealType.class);
            if (realType != null) {
                type = realType.type();
            }

            Method method = BeanUtils.getWriteMethod(object.getClass(), p.getName(), type);
            if (method == null && type.isAssignableFrom(List.class)) {
                method = BeanUtils.getWriteMethod(object.getClass(), p.getName() + "s", type);
            }

            if (method != null) {
                method.invoke(object, p.getValue());
            } else {
                restoring.set(true);
                try {
                    p.setValue(oldValue);
                } finally {
                    restoring.set(false);
                }
                throw new Exception("Cannot update parameter");
            }
        } catch (Exception ex) {
            MessageBox.showMessageDialog(formMain, ex);
        }
    }

    public void edit(TreePath treeNode) {
        this.current = treeNode;
        if (treeNode == null) {
            title.setText("");
            description.setText("");
            properties.setProperties(new Property[]{});
            return;
        }
        DefaultCamelContext context = CamelHelper.createCamelContext();
        RuntimeCamelCatalog catalog = new DefaultRuntimeCamelCatalog(context, true);

        NamedNode o = TreeUtils.getCurrentNode(current);
        String s = catalog.modelJSonSchema(o.getShortName());

        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<String, Object> obj = mapper.readValue(s, Map.class);
            Map<String, Object> model = (Map<String, Object>) obj.get("model");
            title.setText((String) model.get("title"));
            description.setText((String) model.get("description"));

            Map<String, Map<String, String>> props = (Map<String, Map<String, String>>) obj.get("properties");
            List<Property> propertyList = new ArrayList<>();
            for (Map.Entry<String, Map<String, String>> e : props.entrySet()) {
                DefaultProperty p = new DefaultProperty();

                Map<String, String> info = e.getValue();
                p.setName(e.getKey());
                p.setDisplayName(info.get("displayName"));
                p.setMandatory(Boolean.parseBoolean(String.valueOf(info.get("required"))));
                p.setShortDescription(info.get("description"));

                Class clazz = CamelFactory.getType(o, p.getName(), info.get("javaType"), p);

                if (clazz == Removed.class) {
                    continue;
                }
                propertyList.add(p);

                p.setType(clazz);
                if (clazz.isAssignableFrom(BeanString.class)) {
                    p.setUserdata("beanManager", beanManager);
                }

                RealType realType = (RealType) clazz.getAnnotation(RealType.class);
                if (realType != null) {
                    clazz = realType.type();
                }


                Method method = BeanUtils.getReadMethod(o.getClass(), p.getName());
                if (method == null && clazz.isAssignableFrom(List.class))
                    method = BeanUtils.getReadMethod(o.getClass(), p.getName() + "s");

                if (method != null) {
                    Object value = method.invoke(o, (Object[]) null);
                    p.setValue(value);
                }
            }
            Property[] pp = new Property[propertyList.size()];
            propertyList.toArray(pp);
            properties.setProperties(pp);
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

    }

    @Override
    public void enterSimulationMode() {
        properties.setEditable(false);
    }

    @Override
    public void releaseSimulationMode() {
        properties.setEditable(true);
    }
}
