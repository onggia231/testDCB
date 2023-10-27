package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.panel.FileEditor;
import com.telsoft.cbs.designer.panel.FormMain;
import com.telsoft.swing.MessageBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

@Service
public class ReloadAction extends CBAction {
    @Autowired
    private FormMain formMain;

    @Autowired
    private FileEditor fileEditor;

    public ReloadAction() {
        super("Reload");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int code = MessageBox.showConfirmDialog(formMain, "File is modified externally, do you want to reload?", "File is modified externally?", JOptionPane.YES_NO_OPTION);

        switch (code) {
            case JOptionPane.YES_OPTION: {
                open();
                break;
            }
            case JOptionPane.NO_OPTION: {
                fileEditor.setModified();
                break;
            }
        }
    }

    public void open() {
        File curfile = fileEditor.getCurrentFile();
        if (panelDesign.open(curfile)) {
            fileEditor.setCurrentFile(curfile);
            fileEditor.resetMofidied();
        }
    }
}
