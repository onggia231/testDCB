package com.telsoft.cbs.designer.action;

import com.telsoft.cbs.designer.panel.FileEditor;
import com.telsoft.cbs.designer.panel.FormMain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Service
public class SaveAction extends CBAction {
    @Autowired
    private FormMain formMain;

    @Autowired
    private SaveAsAction saveAsAction;

    @Autowired
    private FileEditor fileEditor;

    public SaveAction() {
        super("Save");
        setMnemonic(KeyEvent.VK_S);
        setAccelerator(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        save(e);
    }

    public boolean save(ActionEvent e) {
        if (fileEditor.getCurrentFile() == null) {
            return saveAsAction.saveAs(e);
        } else {
            boolean saved = panelDesign.save(fileEditor.getCurrentFile());
            if (saved)
                fileEditor.resetMofidied();
            return saved;
        }
    }
}
