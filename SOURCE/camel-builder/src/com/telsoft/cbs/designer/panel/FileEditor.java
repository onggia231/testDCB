package com.telsoft.cbs.designer.panel;

import com.telsoft.cbs.designer.action.ReloadAction;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FileEditor {
    @Getter
    private File currentFile = null;
    private long time = -1;

    @Getter
    @Setter
    private boolean readOnly;

    private Timer timer = new Timer();

    @Autowired
    private FormMain formMain;

    @Autowired
    private ReloadAction reloadAction;

    private AtomicBoolean modified = new AtomicBoolean(false);

    public void setModified() {
        modified.set(true);
        updateTitle();
    }

    public boolean isModified() {
        return modified.get();
    }

    public void resetMofidied() {
        modified.set(false);
        if (currentFile != null) {
            time = currentFile.lastModified();
        }
        updateTitle();
    }

    public void setCurrentFile(File currentFile) {
        if (currentFile == null) {
            stopWatching();
        }
        this.currentFile = currentFile;
        if (currentFile != null) {
            startWatching();
        }
        updateTitle();
    }

    private void startWatching() {
        stopWatching();
        time = currentFile.lastModified();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                watching();
            }
        }, 3000, 3000);
    }

    private void stopWatching() {
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }
        timer = null;
    }

    void updateTitle() {
        if (currentFile == null)
            formMain.setTitle("Untitled" + (isModified() ? " *" : ""));
        else
            formMain.setTitle(currentFile.getName() + (isModified() ? " *" : ""));
    }

    public void watching() {
        File file = new File(currentFile.getAbsolutePath());
        long newTime = file.lastModified();
        if (newTime != time) {
            stopWatching();
            reloadAction.actionPerformed(null);
        }
    }
}
