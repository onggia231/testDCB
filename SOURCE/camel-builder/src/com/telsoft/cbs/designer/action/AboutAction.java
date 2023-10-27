package com.telsoft.cbs.designer.action;

import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class AboutAction extends CBAction {
    public AboutAction() {
        super("About");
        setMnemonic(KeyEvent.VK_A);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }
}
