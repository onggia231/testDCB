package com.telsoft.cbs.designer.utils;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;

public class RedirectOutputStream extends OutputStream {
    private final JTextArea txtLogging;
    private StringBuffer buf = new StringBuffer();
    private int maxLength = 30000;

    public RedirectOutputStream(JTextArea txtLogging) {
        this.txtLogging = txtLogging;
    }

    public synchronized void write(int b) throws IOException {
        buf.append((char) b);

        if (buf.length() > maxLength) {
            int overLength = buf.length() - maxLength;
            buf.delete(0, overLength);
        }


        txtLogging.setText(buf.toString());
        int iLen = buf.length();
        txtLogging.setCaretPosition(iLen);
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        buf.append(new String(b, off, len));

        if (buf.length() > maxLength) {
            int overLength = buf.length() - maxLength;
            buf.delete(0, overLength);
        }


        txtLogging.setText(buf.toString());
        int iLen = buf.length();
        txtLogging.setCaretPosition(iLen);
    }

    @Override
    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }
}
