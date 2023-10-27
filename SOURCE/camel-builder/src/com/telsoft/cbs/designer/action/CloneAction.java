package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.Registry;
import com.telsoft.cbs.designer.utils.TreeUtils;
import com.telsoft.cbs.loader.ModelHelper;
import com.telsoft.swing.MessageBox;
import org.apache.camel.NamedNode;
import org.apache.camel.model.OptionalIdentifiedDefinition;
import org.springframework.stereotype.Service;

import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;
import java.io.*;

@Service
public class CloneAction extends CBAction {
    public CloneAction() {
        super("Clone");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        NamedNode child = TreeUtils.getCurrentNode(getPanelDesign().getCurrentPath());
        NamedNode parent = TreeUtils.getCurrentParent(getPanelDesign().getCurrentPath());

        NamedNode cloned = null;
        try {
            cloned = NamedNodeLoader.clone(child);
            NodeHelper parentHelper = Registry.getHelper(parent);
            parentHelper.insertChild(getPanelDesign().getTree(), parent, cloned, -1);
        } catch (Exception ex) {
            MessageBox.showMessageDialog(ex);
        }
    }

    @Override
    public void updateStatus(TreePath currentTreeNode, NodeHelper helper) {
        setEnabled(helper.isCloneable());
    }

    private static class NamedNodeLoader {
        static NamedNode clone(NamedNode source) throws Exception {
            NamedNodeLoader loader = new NamedNodeLoader();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            loader.save(source, out);
            NamedNode result = loader.load(new ByteArrayInputStream(out.toByteArray()));
            if (result instanceof OptionalIdentifiedDefinition)
                TreeUtils.process((OptionalIdentifiedDefinition) result, node -> {
                    // reset id
                    node.setId(null);
                    node.setCustomId(false);
                });
            return result;
        }

        public NamedNode load(InputStream in) throws Exception {
            return com.telsoft.cbs.loader.ModelHelper.createModelFromXml(null, in, NamedNode.class);
        }

        public void save(NamedNode namedNode, OutputStream out) throws Exception {
            try (BufferedOutputStream bout = new BufferedOutputStream(out)) {
                String s = ModelHelper.dumpModelAsXml(null, namedNode);
                bout.write(s.getBytes());
            }
        }
    }
}
