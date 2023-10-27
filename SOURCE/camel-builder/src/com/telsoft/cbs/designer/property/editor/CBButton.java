package com.telsoft.cbs.designer.property.editor;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

public class CBButton extends JButton {
    @Getter
    @Setter
    Object userObject;

    public CBButton(String text) {
        super(text);
    }

    public CBButton() {
        super();
    }
}
