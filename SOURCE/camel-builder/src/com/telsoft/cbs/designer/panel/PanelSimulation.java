package com.telsoft.cbs.designer.panel;

import com.l2fprod.common.propertysheet.DefaultProperty;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.propertysheet.datatype.ListString;
import com.telsoft.cbs.designer.action.ActionList;
import com.telsoft.cbs.designer.property.CamelFactory;
import com.telsoft.cbs.designer.property.type.Bean;
import com.telsoft.cbs.designer.property.type.OneClass;
import com.telsoft.cbs.designer.utils.MyAppender;
import com.telsoft.swing.MessageBox;
import com.telsoft.util.AppException;
import com.telsoft.util.Global;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.model.FromDefinition;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.beans.PropertyEditor;
import java.util.Arrays;
import java.util.Vector;

@Component
@Service
public class PanelSimulation extends CBPanel {
    private static final Level[] levels = new Level[]{
            Level.FATAL,
            Level.ERROR,
            Level.WARN,
            Level.INFO,
            Level.DEBUG,
            Level.TRACE
    };

    private final CamelFactory factory = new CamelFactory();
    @Autowired
    private ActionList actionList;
    @Autowired
    private BeanManager beanManager;
    @Autowired
    private PanelDesign panelDesign;
    private FormMain formMain;
    private JTextArea txtLogging = new JTextArea();
    private JPanel panelDebug = new JPanel();
    private PropertySheetPanel propertyDebug = new PropertySheetPanel();
    private DefaultProperty propertyPattern = new DefaultProperty();
    private DefaultProperty propertyInput = new DefaultProperty();
    private DefaultProperty propertyClass = new DefaultProperty();
    private DefaultProperty propertyMessage = new DefaultProperty();
    private final Property[] properties = new Property[]{
            propertyPattern,
            propertyInput,
            propertyClass,
            propertyMessage
    };
    private CamelDebugger debugger = null;

    public PanelSimulation(@Autowired FormMain owner) {
        this.formMain = owner;
    }

    public void init() {
        setLayout(new BorderLayout());
        JScrollPane pnlLogging = new JScrollPane(txtLogging);
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.add(actionList.getRunAction());
        toolBar.add(actionList.getStopAction());
        toolBar.addSeparator();
        toolBar.add(new JLabel("Log Level "));

        JComboBox comboBox = new JComboBox(new Vector(Arrays.asList(levels)));
        toolBar.add(comboBox);

        add(toolBar, BorderLayout.NORTH);
        add(pnlLogging, BorderLayout.CENTER);
        add(panelDebug, BorderLayout.SOUTH);
        txtLogging.setBorder(BorderFactory.createCompoundBorder());

        txtLogging.setEditable(false);
        txtLogging.setWrapStyleWord(false);
        txtLogging.setTabSize(4);
        txtLogging.setBackground(new Color(96, 96, 128));
        txtLogging.setForeground(Color.WHITE);

        JPopupMenu menu;
        txtLogging.setComponentPopupMenu(menu = new JPopupMenu("Logging"));
        menu.add("Clear").addActionListener(e -> {
            txtLogging.setText("");
        });

        panelDebug.setBorder(new TitledBorder("Debugging"));
        panelDebug.setLayout(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(2, 2, 2, 2), 2, 2);

        panelDebug.add(propertyDebug, constraints);
        propertyDebug.setPreferredSize(new Dimension(120, 120));
        CamelFactory.registerTo(propertyDebug);
        propertyPattern.setDisplayName("Exchange Pattern");
        propertyPattern.setName("exchangePattern");
        propertyPattern.setType(ExchangePattern.class);

        propertyInput.setDisplayName("Input");
        propertyInput.setName("input");
        propertyInput.setType(ListString.class);

        propertyClass.setDisplayName("Message Class");
        propertyClass.setName("class");
        propertyClass.setType(OneClass.class);
        propertyClass.addPropertyChangeListener(evt -> {
            try {
                Class clazz = Class.forName((String) evt.getNewValue());
                PropertyEditor editor = factory.getEditorFactory().getEditor(clazz);
                if (editor == null) {
                    propertyMessage.setType(Bean.class);
                    propertyMessage.setUserdata("CLASS", clazz);
                } else
                    propertyMessage.setType(clazz);
            } catch (ClassNotFoundException e) {
                propertyMessage.setType(Void.class);
            }
        });

        propertyMessage.setDisplayName("Message");
        propertyMessage.setName("message");
        propertyMessage.setType(Void.class);

        propertyDebug.setProperties(properties);

        JButton buttonSend;
        constraints.gridx++;
        constraints.fill = GridBagConstraints.NONE;
        panelDebug.add(buttonSend = new JButton("Send"), constraints);

        buttonSend.addActionListener(e -> {
            new Thread(() -> sendRequest(propertyMessage.getValue(),
                    (String) propertyInput.getValue(),
                    (ExchangePattern) propertyPattern.getValue())).start();
        });

        setSize(1000, 700);
        BasicConfigurator.configure(new MyAppender(txtLogging));
        Logger.getRootLogger().setLevel(Level.INFO);
        Logger.getLogger(Reflections.class).setLevel(Level.OFF);

        comboBox.setSelectedItem(Logger.getRootLogger().getLevel());
        comboBox.addActionListener(e -> Logger.getRootLogger().setLevel((Level) comboBox.getSelectedItem()));
        actionList.normalMode();
    }

    @Override
    public void enterSimulationMode() {

    }

    @Override
    public void releaseSimulationMode() {

    }

    protected void sendRequest(Object body, String endpointUri, ExchangePattern exchangePattern) {
        if (debugger != null) {
            Exchange exchange = new DefaultExchange(debugger.context(), exchangePattern);
            exchange.getIn().setBody(body);
            debugger.template().send(endpointUri, exchange);
        }
    }

    private void run0(boolean debug) {
        if (!isRunning()) {
            try {
                actionList.debugMode();
                debugger = new CamelDebugger(panelDesign, beanManager, debug);
                debugger.start();

                java.util.List<RouteDefinition> routes = panelDesign.getRoutesDefinition().getRoutes();

                Vector<String> inputs = new Vector<>();
                for (RouteDefinition route : routes) {
                    FromDefinition from = route.getInput();
                    if (from != null) {
                        inputs.add(from.getUri());
                    }
                }
                propertyInput.setUserdata("LIST", inputs);
            } catch (Throwable ex) {
                actionList.normalMode();
                MessageBox.showMessageDialog(formMain, new AppException(new Exception(ex), "Run"), Global.APP_NAME, MessageBox.ERROR_MESSAGE);
                stop();
            }
        }
    }

    public void run() {
        formMain.enterSimulationMode();
        run0(false);
    }

    public void debug() {
        formMain.enterSimulationMode();
        run0(true);
    }

    public boolean isRunning() {
        return debugger != null && debugger.isRunning();
    }

    public void stop() {
        if (isRunning()) {
            try {
                debugger.stop();
                actionList.normalMode();
            } catch (Exception e) {
                actionList.debugMode();
                e.printStackTrace();
            }
            debugger = null;
        }
        formMain.releaseSimulationMode();
    }
}
