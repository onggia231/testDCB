package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.panel.FileEditor;
import com.telsoft.cbs.designer.panel.FormMain;
import com.telsoft.swing.MessageBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

@Service
public class OpenAction extends CBAction {
    @Autowired
    private FormMain formMain;

    @Autowired
    private FileEditor fileEditor;

    @Autowired
    private SaveAction saveAction;

    public OpenAction() {
        super("Open");
        setMnemonic(KeyEvent.VK_O);
        setAccelerator(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int code = JOptionPane.NO_OPTION;
        if (fileEditor.isModified()) {
            code = MessageBox.showConfirmDialog(formMain, "File is modified, do you want to save?", "Save file?", JOptionPane.YES_NO_CANCEL_OPTION);
        }

        switch (code) {
            case JOptionPane.NO_OPTION: {
                open();
                break;
            }
            case JOptionPane.CANCEL_OPTION:
                break;

            case JOptionPane.YES_OPTION: {
                if (saveAction.save(e)) {
                    open();
                    break;
                }
            }
        }
    }

    public void open() {
        File curfile = fileEditor.getCurrentFile();
        String curDir = curfile == null ? "./" : curfile.getAbsolutePath();
        JFileChooser openDialog = new JFileChooser(curDir);
        openDialog.setDialogType(JFileChooser.OPEN_DIALOG);
        int result = openDialog.showOpenDialog(formMain);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = openDialog.getSelectedFile();
            if (panelDesign.open(file)) {
                fileEditor.setCurrentFile(file);
                fileEditor.resetMofidied();
            }
        }
    }
}
