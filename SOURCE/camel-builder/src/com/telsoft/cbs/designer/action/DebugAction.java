package com.telsoft.cbs.designer.action;

import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class DebugAction extends CBAction {
    public DebugAction() {
        super("Debug");
        setMnemonic(KeyEvent.VK_D);
        setAccelerator(KeyEvent.VK_F5, KeyEvent.CTRL_DOWN_MASK);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getPanelSimulation().debug();
    }
}
