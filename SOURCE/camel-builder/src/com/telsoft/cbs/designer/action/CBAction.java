package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.PanelDesign;
import com.telsoft.cbs.designer.panel.PanelSimulation;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.tree.TreePath;

public abstract class CBAction extends AbstractAction {

    @Autowired
    @Getter
    PanelDesign panelDesign;

    @Autowired
    @Getter
    PanelSimulation panelSimulation;

    public CBAction() {
    }

    public CBAction(String name, Icon icon) {
        super(name, icon);
    }

    public CBAction(String name) {
        super(name);
    }

    public int getMnemonic() {
        return (int) getValue(Action.MNEMONIC_KEY);
    }

    public void setMnemonic(int keyCode) {
        putValue(Action.MNEMONIC_KEY, keyCode);
    }

    public void setAccelerator(int keyCode, int keyModifiers) {
        KeyStroke keyStroke = KeyStroke.getKeyStroke(keyCode, keyModifiers);
        putValue(Action.ACCELERATOR_KEY, keyStroke);
    }

    public void updateStatus(TreePath currentTreeNode, NodeHelper helper) {

    }

    public void updateTree() {
        PanelDesign.CBTree tree = getPanelDesign().getTree();
        tree.treeDidChange();
        tree.updateUI();
        tree.getFileEditor().setModified();
    }
}
