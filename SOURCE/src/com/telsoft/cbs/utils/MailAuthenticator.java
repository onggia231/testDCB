/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.telsoft.cbs.utils;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

public class MailAuthenticator extends Authenticator {

    String username;
    String pass;
    public MailAuthenticator(String username, String pass) {
        this.username = username;
        this.pass = pass;
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, pass);
    }
    
}
