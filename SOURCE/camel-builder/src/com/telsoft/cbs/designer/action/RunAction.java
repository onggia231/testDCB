package com.telsoft.cbs.designer.action;

import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class RunAction extends CBAction {
    public RunAction() {
        super("Run");
        setMnemonic(KeyEvent.VK_R);
        setAccelerator(KeyEvent.VK_F5, 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        getPanelSimulation().run();
    }
}
