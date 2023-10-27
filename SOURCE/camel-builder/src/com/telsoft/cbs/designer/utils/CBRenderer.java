package com.telsoft.cbs.designer.utils;

import com.telsoft.cbs.designer.nodes.NodeHelper;
import com.telsoft.cbs.designer.panel.PanelDesign;
import com.telsoft.cbs.designer.panel.Registry;
import org.apache.camel.NamedNode;

import javax.swing.*;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * <p>Title: Thread Monitor</p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2009</p>
 *
 * <p>Company: TELSOFT</p>
 *
 * @author Nguyen Cong Khanh
 * @version 1.0
 */
public class CBRenderer extends JLabel implements TreeCellRenderer {

    private final PanelDesign panelDesign;

    /**
     * @param panelDesign
     */
    public CBRenderer(PanelDesign panelDesign) {
        setOpaque(true);
        this.panelDesign = panelDesign;
    }

    /**
     * @param tree     JTree
     * @param value    Object
     * @param selected boolean
     * @param expanded boolean
     * @param leaf     boolean
     * @param row      int
     * @param hasFocus boolean
     * @return Component
     */
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        setEnabled(tree.isEnabled());
        setComponentOrientation(tree.getComponentOrientation());

        Color background = (selected ? SystemColor.textHighlight : SystemColor.white);
        Color foreground = (selected ? SystemColor.textHighlightText : SystemColor.controlText);
        Icon icon = null;

        NodeHelper me = Registry.getHelper((NamedNode) value);
        if (me != null) {
            try {
                if (panelDesign.isMarked((NamedNode) value)) {
                    background = Color.YELLOW;
                    foreground = Color.black;
                } else {
                    background = me.getColor((NamedNode) value, false, selected, expanded, leaf, hasFocus);
                    foreground = me.getColor((NamedNode) value, true, selected, expanded, leaf, hasFocus);
                }

                icon = me.getIcon((NamedNode) value, selected, expanded, leaf, hasFocus);
                setText("<html>" + me.onGetText((NamedNode) value) + "</html>");
            } catch (Exception ex) {
                setText(value.toString());
            }
        } else {
            setText(value.toString());
        }
        setBackground(background);
        setForeground(foreground);
        setIcon(icon);
        return this;
    }
}
