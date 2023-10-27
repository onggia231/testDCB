package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.panel.FileEditor;
import com.telsoft.cbs.designer.panel.FormMain;
import com.telsoft.swing.MessageBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class ExitAction extends CBAction {
    @Autowired
    private FormMain formMain;

    @Autowired
    private FileEditor fileEditor;

    @Autowired
    private SaveAction saveAction;

    public ExitAction() {
        super("Exit");
        setMnemonic(KeyEvent.VK_X);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int code = JOptionPane.NO_OPTION;
        if (fileEditor.isModified()) {
            code = MessageBox.showConfirmDialog(formMain, "File is modified, do you want to save?", "Save file?", JOptionPane.YES_NO_CANCEL_OPTION);
        }

        switch (code) {
            case JOptionPane.NO_OPTION: {
                formMain.exit();
                break;
            }
            case JOptionPane.CANCEL_OPTION:
                break;

            case JOptionPane.YES_OPTION: {
                if (saveAction.save(e)) {
                    panelDesign.reset();
                    fileEditor.setCurrentFile(null);
                    fileEditor.resetMofidied();
                    break;
                }
            }
        }
    }
}
