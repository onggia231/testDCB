package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.Registry;
import com.telsoft.cbs.designer.utils.TreeUtils;
import org.apache.camel.NamedNode;
import org.springframework.stereotype.Service;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class RemoveAction extends CBAction {
    public RemoveAction() {
        super("Remove");
        setMnemonic(KeyEvent.VK_R);
        setAccelerator(KeyEvent.VK_DELETE, 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(getPanelDesign().getCurrentPath());

        NodeHelper helper = Registry.getHelper(parent);
        if (helper != null) {
            helper.removeChild(panelDesign.getTree(), parent, child);
        }
    }

    @Override
    public void updateStatus(TreePath currentTreeNode, NodeHelper helper) {
        setEnabled(helper.isRemovable());
    }
}
