package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.Registry;
import com.telsoft.cbs.designer.utils.TreeUtils;
import org.apache.camel.NamedNode;
import org.springframework.stereotype.Service;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;

@Service
public class MoveUpAction extends CBAction {

    public MoveUpAction() {
        super("Move up");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(getPanelDesign().getCurrentPath());

        if (parent != null) {
            NodeHelper parentNodeHelper = Registry.getHelper(parent);
            int i = parentNodeHelper.getIndex(parent, child);
            parentNodeHelper.removeChild(this.panelDesign.getTree(), parent, child);
            parentNodeHelper.insertChild(this.panelDesign.getTree(), parent, child, i - 1);
            updateTree();
        }
    }

    @Override
    public void updateStatus(TreePath currentTreeNode, NodeHelper helper) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(currentTreeNode);
        if (parent == null) {
            setEnabled(false);
        } else {
            NodeHelper parentNodeHelper = Registry.getHelper(parent);
            int i = parentNodeHelper.getIndex(parent, child);
            setEnabled(parentNodeHelper.canInsertAt(parent, child, i - 1));
        }
    }
}
