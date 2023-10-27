package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.nodes.NamedNodeMenuItem;
import com.telsoft.cbs.designer.panel.FileEditor;
import org.apache.camel.model.OptionalIdentifiedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;

@Service
public class RoutesResetIdsAction extends CBAction {
    @Autowired
    private FileEditor fileEditor;

    public RoutesResetIdsAction() {
        super("Reset node ids");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o instanceof NamedNodeMenuItem) {
            OptionalIdentifiedDefinition node = (OptionalIdentifiedDefinition) ((NamedNodeMenuItem) o).getCurrentTreeNode();
            node.setId(null);
            node.setCustomId(false);
            fileEditor.setModified();
        }
    }
}
