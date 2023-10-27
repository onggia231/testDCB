package com.telsoft.cbs.designer.nodes;

import lombok.Getter;
import lombok.Setter;
import org.apache.camel.NamedNode;

import javax.swing.*;

public class NamedNodeMenuItem extends JMenuItem {
    @Getter
    @Setter
    private NamedNode currentTreeNode;

    @Getter
    @Setter
    private JTree tree;
}
