package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.designer.action.ActionList;
import com.telsoft.dictionary.DefaultDictionary;
import com.telsoft.swing.JXFrame;
import com.telsoft.swing.Skin;
import com.telsoft.swing.WindowManager;
import jiconfont.icons.google_material_design_icons.GoogleMaterialDesignIcons;
import jiconfont.swing.IconFontSwing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.awt.event.KeyEvent;

@Component
public class FormMain extends JXFrame {
    private JSplitPane splitMain = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    @Autowired
    private ActionList actionList;
    @Autowired
    private PanelDesign panelDesign;
    @Autowired
    private PanelEditor panelEditor;
    @Autowired
    private PanelBeanManager panelBeanManager;
    @Autowired
    private PanelSimulation panelSimulation;
    @Autowired
    private FileEditor fileEditor;
    private JMenuBar mainMenu = new JMenuBar();
    private JSplitPane splitPane = new JSplitPane();

    public FormMain() {
        super();
        IconFontSwing.register(GoogleMaterialDesignIcons.getIconFont());
    }

    public static void setUIFont(FontUIResource f) {
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, f);
            }
        }
    }

    @Override
    public void setTitle(String title) {
        super.setTitle("Telsoft Camel Designer - " + title);
    }

    @Override
    public void onClosing() {
        actionList.getExitAction().actionPerformed(null);
    }

    public void formInit() {
        this.panelBeanManager.init();
        this.panelDesign.init();
        this.panelEditor.init();
        this.panelSimulation.init();

        FontUIResource fui = new FontUIResource(Skin.FONT_COMMON);
        setUIFont(fui);
        DefaultDictionary.setCurrentLanguage("EN");

        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.setTitle("");
        this.setLayout(new BorderLayout());

        splitPane.setLeftComponent(panelDesign);
        splitPane.setRightComponent(panelEditor);
        splitPane.setDividerLocation(700);
        splitPane.setDividerSize(6);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Routes", splitPane);
        tabbedPane.addTab("Beans", panelBeanManager);

        this.add(splitMain, BorderLayout.CENTER);
        splitMain.setTopComponent(tabbedPane);
        splitMain.setBottomComponent(null);

        setPreferredSize(new Dimension(1000, 720));

        setJMenuBar(mainMenu);
        JMenu fileMenu = mainMenu.add(new JMenu("File"));
        fileMenu.setMnemonic(KeyEvent.VK_F);

        fileMenu.add(actionList.getNewAction());
        fileMenu.add(actionList.getOpenAction());
        fileMenu.add(actionList.getSaveAction());
        fileMenu.add(actionList.getSaveAsAction());
        fileMenu.addSeparator();
        fileMenu.add(actionList.getExitAction());

        JMenu simulationMenu = mainMenu.add(new JMenu("Simulation"));
        simulationMenu.setMnemonic(KeyEvent.VK_S);
        simulationMenu.add(actionList.getRunAction());
        simulationMenu.add(actionList.getDebugAction());
        simulationMenu.add(actionList.getStopAction());

        JMenu toolMenu = mainMenu.add(new JMenu("Tools"));
        toolMenu.setMnemonic(KeyEvent.VK_T);
        toolMenu.add(actionList.getExportBeanAction());

        JMenu helpMenu = mainMenu.add(new JMenu("Help"));
        helpMenu.setMnemonic(KeyEvent.VK_H);
        helpMenu.add(actionList.getAboutAction());

        fileEditor.setCurrentFile(null);
        fileEditor.resetMofidied();
        Skin.applySkin(mainMenu);
        Skin.applySkin(this);
    }

    public void run(String[] args) {
        this.pack();
        WindowManager.centeredWindow(this);
    }

    public void enterSimulationMode() {
        splitMain.setBottomComponent(panelSimulation);
        fileEditor.setReadOnly(true);

        this.panelBeanManager.enterSimulationMode();
        this.panelDesign.enterSimulationMode();
        this.panelEditor.enterSimulationMode();
        this.panelSimulation.enterSimulationMode();
    }

    public void releaseSimulationMode() {
        splitMain.setBottomComponent(null);
        fileEditor.setReadOnly(false);

        this.panelBeanManager.releaseSimulationMode();
        this.panelDesign.releaseSimulationMode();
        this.panelEditor.releaseSimulationMode();
        this.panelSimulation.releaseSimulationMode();
    }
}
