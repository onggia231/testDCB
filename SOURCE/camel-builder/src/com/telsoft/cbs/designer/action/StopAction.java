package com.telsoft.cbs.designer.action;

import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class StopAction extends CBAction {
    public StopAction() {
        super("Stop");
        setMnemonic(KeyEvent.VK_S);
        setAccelerator(KeyEvent.VK_F6, 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getPanelSimulation().stop();
    }
}
