package org.apache.commons.mail;

import javax.mail.Session;
import java.util.Properties;

public class HtmlEmailSmtps extends HtmlEmail {


    private Session session;

    public Session getMailSession() throws EmailException {
        Session session = super.getMailSession();
//        Properties prop = session.getProperties();
//        prop.setProperty("mail.transport.protocol","smtps");
        if(!session.getProperty("mail.transport.protocol").equalsIgnoreCase("smtps")) {
            session.getProperties().setProperty("mail.transport.protocol", "smtps");
            super.setMailSession(session);
        }
        if (this.session == null) {
            Properties properties = new Properties(System.getProperties());
            properties.setProperty("mail.transport.protocol", "smtp");
            if (EmailUtils.isEmpty(this.hostName)) {
                this.hostName = properties.getProperty("mail.smtp.host");
            }

            if (EmailUtils.isEmpty(this.hostName)) {
                throw new EmailException("Cannot find valid hostname for mail session");
            }

            properties.setProperty("mail.smtp.port", this.smtpPort);
            properties.setProperty("mail.smtp.host", this.hostName);
            properties.setProperty("mail.debug", String.valueOf(this.debug));
            properties.setProperty("mail.smtp.starttls.enable", this.isStartTLSEnabled() ? "true" : "false");
            properties.setProperty("mail.smtp.starttls.required", this.isStartTLSRequired() ? "true" : "false");
            properties.setProperty("mail.smtp.sendpartial", this.isSendPartial() ? "true" : "false");
            properties.setProperty("mail.smtps.sendpartial", this.isSendPartial() ? "true" : "false");
            if (this.authenticator != null) {
                properties.setProperty("mail.smtp.auth", "true");
            }

            if (this.isSSLOnConnect()) {
                properties.setProperty("mail.smtp.port", this.sslSmtpPort);
                properties.setProperty("mail.smtp.socketFactory.port", this.sslSmtpPort);
                properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                properties.setProperty("mail.smtp.socketFactory.fallback", "false");
            }

            if ((this.isSSLOnConnect() || this.isStartTLSEnabled()) && this.isSSLCheckServerIdentity()) {
                properties.setProperty("mail.smtp.ssl.checkserveridentity", "true");
            }

            if (this.bounceAddress != null) {
                properties.setProperty("mail.smtp.from", this.bounceAddress);
            }

            if (this.socketTimeout > 0) {
                properties.setProperty("mail.smtp.timeout", Integer.toString(this.socketTimeout));
            }

            if (this.socketConnectionTimeout > 0) {
                properties.setProperty("mail.smtp.connectiontimeout", Integer.toString(this.socketConnectionTimeout));
            }

            this.session = Session.getInstance(properties, this.authenticator);
        }

        return this.session;
    }
}
