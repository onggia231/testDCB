package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.panel.FileEditor;
import com.telsoft.cbs.designer.panel.FormMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

@Service
public class SaveAsAction extends CBAction {
    @Autowired
    FormMain formMain;

    @Autowired
    private FileEditor fileEditor;

    public SaveAsAction() {
        super("Save as ...");
        setMnemonic(KeyEvent.VK_A);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        saveAs(e);
    }

    public boolean saveAs(ActionEvent e) {
        File curfile = fileEditor.getCurrentFile();
        String curDir = curfile == null ? "./" : curfile.getAbsolutePath();
        JFileChooser saveDialog = new JFileChooser(curDir);
        saveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
        int result = saveDialog.showSaveDialog(formMain);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = saveDialog.getSelectedFile();
            boolean saved = panelDesign.save(file);
            if (saved) {
                fileEditor.setCurrentFile(file);
                fileEditor.resetMofidied();
            }
            return saved;
        }
        return false;
    }
}
