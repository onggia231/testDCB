package com.telsoft.cbs.designer.dialog;

import com.telsoft.swing.MessageBox;
import com.telsoft.swing.Skin;
import com.telsoft.swing.WindowManager;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.awt.*;

public abstract class BaseDialog extends JDialog {
    @Getter
    @Setter
    private int code;

    private JPanel panelButton = new JPanel();
    private GridBagConstraints gbcButton = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(2, 2, 2, 2), 2, 2);

    public BaseDialog(Frame parent, boolean modal) {
        super(parent, modal);
    }

    private void initGui() {
        setTitle(getTitleText());
        setLayout(new BorderLayout());
        JPanel panelList = new JPanel();
        add(panelList, BorderLayout.CENTER);
        add(panelButton, BorderLayout.SOUTH);
        panelButton.setLayout(new GridBagLayout());

        initButton();

        setSize(900, 500);
        setResizable(true);

        initContent(panelList);
        Skin.applySkin(this);
    }

    protected void addButton(JButton button) {
        panelButton.add(button, gbcButton);
        button.setPreferredSize(Skin.BUTTON_MIN_SIZE);
        gbcButton.gridx++;
    }

    protected void initButton() {
        JButton buttonOK = new JButton(getOKText());
        JButton buttonCancel = new JButton(getCancelText());
        addButton(buttonOK);
        addButton(buttonCancel);

        buttonCancel.addActionListener(e -> {
            onCancel();
        });

        buttonOK.addActionListener(e -> {
            onOK();
        });
    }

    protected String getTitleText() {
        return "Choose...";
    }

    protected void initContent(JPanel panelContent) {

    }

    protected void onOK() {
        code = JOptionPane.OK_OPTION;
        BaseDialog.this.dispose();
    }

    protected void onCancel() {
        code = JOptionPane.CANCEL_OPTION;
        BaseDialog.this.dispose();
    }

    protected String getCancelText() {
        return "Cancel";
    }

    protected String getOKText() {
        return "OK";
    }

    public void init() {
        initGui();
    }

    public int showDialog() {
        WindowManager.centeredWindow(this);
        return code;
    }

    public void showMessage(Exception e) {
        MessageBox.showMessageDialog(this.getParent(), e);
    }

}
