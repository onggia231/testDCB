package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.Registry;
import com.telsoft.cbs.designer.utils.TreeUtils;
import org.apache.camel.NamedNode;
import org.springframework.stereotype.Service;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

@Service
public class MoveDownAction extends CBAction {
    public MoveDownAction() {
        super("Move down");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(getPanelDesign().getCurrentPath());

        if (parent != null) {
            NodeHelper parentHelper = Registry.getHelper(parent);
            int i = parentHelper.getIndex(parent, child);
            parentHelper.removeChild(panelDesign.getTree(), parent, child);
            parentHelper.insertChild(panelDesign.getTree(), parent, child, i + 1);
            updateTree();
        }
    }

    @Override
    public void updateStatus(TreePath currentTreeNode, NodeHelper helper) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(getPanelDesign().getCurrentPath());

        if (parent == null) {
            setEnabled(false);
        } else {
            NodeHelper parentHelper = Registry.getHelper(parent);
            int i = parentHelper.getIndex(parent, child);
            setEnabled(parentHelper.canInsertAt(parent, child, i + 1));
        }
    }

}
