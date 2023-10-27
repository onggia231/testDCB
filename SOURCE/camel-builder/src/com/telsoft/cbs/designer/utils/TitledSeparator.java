package com.telsoft.cbs.designer.utils;

import javax.swing.*;
import java.awt.*;

public class TitledSeparator extends JMenuItem {
    public TitledSeparator(String text) {
        super();
        setText("<html><font style='bold' color='white'>" + text + "</font></html>");
        setBackground(Color.gray.brighter());
        setEnabled(false);
    }
}
