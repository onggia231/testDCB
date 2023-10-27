package com.telsoft.cbs.designer.panel;

import com.telsoft.swing.JXPanel;

public abstract class CBPanel extends JXPanel {
    public void init() {

    }

    public abstract void enterSimulationMode();

    public abstract void releaseSimulationMode();
}
