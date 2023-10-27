package com.telsoft.cbs.designer.dialog;

import com.telsoft.cbs.designer.utils.ComponentInfo;
import sun.swing.DefaultLookup;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Map;

public class ComponentCellRenderer implements javax.swing.ListCellRenderer<ComponentInfo> {
    private static final Border SAFE_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    private static final Border DEFAULT_NO_FOCUS_BORDER = new EmptyBorder(1, 1, 1, 1);
    protected static Border noFocusBorder = DEFAULT_NO_FOCUS_BORDER;

    public ComponentCellRenderer() {
    }

    private Border getNoFocusBorder(JPanel panel) {
        Border border = DefaultLookup.getBorder(panel, panel.getUI(), "List.cellNoFocusBorder");
        if (System.getSecurityManager() != null) {
            if (border != null) return border;
            return SAFE_NO_FOCUS_BORDER;
        } else {
            if (border != null &&
                    (noFocusBorder == null ||
                            noFocusBorder == DEFAULT_NO_FOCUS_BORDER)) {
                return border;
            }
            return noFocusBorder;
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ComponentInfo> list,
                                                  ComponentInfo value,
                                                  int index,
                                                  boolean isSelected,
                                                  boolean cellHasFocus) {

        JPanel panel = new JPanel();
        panel.setComponentOrientation(list.getComponentOrientation());
        panel.setOpaque(true);
        panel.setBorder(getNoFocusBorder(panel));

        Color bg = null;
        Color fg = null;

        JList.DropLocation dropLocation = list.getDropLocation();
        if (dropLocation != null
                && !dropLocation.isInsert()
                && dropLocation.getIndex() == index) {

            bg = DefaultLookup.getColor(panel, panel.getUI(), "List.dropCellBackground");
            fg = DefaultLookup.getColor(panel, panel.getUI(), "List.dropCellForeground");

            isSelected = true;
        }

        JLabel text = new JLabel();
        JLabel description = new JLabel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1.0, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0);
        panel.add(text, gbc);
        gbc.gridy++;
        panel.add(description, gbc);

        if (isSelected) {
            text.setBackground(bg == null ? list.getSelectionBackground() : bg);
            text.setForeground(fg == null ? list.getSelectionForeground() : fg);
            description.setBackground(bg == null ? list.getSelectionBackground() : bg);
            description.setForeground(fg == null ? list.getSelectionForeground() : fg);
            panel.setBackground(bg == null ? list.getSelectionBackground() : bg);
            panel.setForeground(fg == null ? list.getSelectionForeground() : fg);
        } else {
            text.setBackground(list.getBackground());
            text.setForeground(list.getForeground());
            description.setBackground(list.getBackground());
            description.setForeground(list.getForeground());
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        if (value != null) {
            text.setText("<html><b>" + value.getName() + "</b></html>");
            if (value.getSchema() != null) {
                Map<String, Object> props = (Map<String, Object>) value.getSchema().get("component");
                description.setText((String) props.get("description") + " ");
            }
        } else {
            text.setText("");
            description.setText("");
        }

        panel.setEnabled(list.isEnabled());
        text.setEnabled(list.isEnabled());
        description.setEnabled(list.isEnabled());
        panel.setFont(list.getFont());
        text.setFont(list.getFont());
        description.setFont(list.getFont());

        Border border = null;
        if (cellHasFocus) {
            if (isSelected) {
                border = DefaultLookup.getBorder(panel, list.getUI(), "List.focusSelectedCellHighlightBorder");
            }
            if (border == null) {
                border = DefaultLookup.getBorder(panel, list.getUI(), "List.focusCellHighlightBorder");
            }
        } else {
            border = getNoFocusBorder(panel);
        }
        panel.setBorder(border);
        text.setBorder(noFocusBorder);
        description.setBorder(noFocusBorder);
        return panel;
    }
}
