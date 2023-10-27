package com.telsoft.cbs.designer.action;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Getter
@Service
public final class ActionList {
    @Autowired
    private CloneAction cloneAction;
    @Autowired
    private ExitAction exitAction;
    @Autowired
    private ExportBeanAction exportBeanAction;
    @Autowired
    private MoveDownAction moveDownAction;
    @Autowired
    private MoveUpAction moveUpAction;
    @Autowired
    private NewAction newAction;
    @Autowired
    private OpenAction openAction;
    @Autowired
    private RemoveAction removeAction;
    @Autowired
    private RunAction runAction;
    @Autowired
    private SaveAction saveAction;
    @Autowired
    private SaveAsAction saveAsAction;
    @Autowired
    private StopAction stopAction;
    @Autowired
    private DebugAction debugAction;
    @Autowired
    private AboutAction aboutAction;

    @Autowired
    private RoutesResetIdsAction routesResetIdsAction;

    private ActionList() {

    }

    public void debugMode() {
        getRunAction().setEnabled(false);
        getDebugAction().setEnabled(false);
        getStopAction().setEnabled(true);
    }

    public void normalMode() {
        getRunAction().setEnabled(true);
        getDebugAction().setEnabled(true);
        getStopAction().setEnabled(false);
    }
}
