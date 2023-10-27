package com.telsoft.cbs.utils;


import com.telsoft.util.SmartZip;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.HtmlEmailSmtps;
import org.apache.commons.mail.MultiPartEmail;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by T430 on 11/11/2015.
 */
public class MailServicesUtil {
//    private static String mstrHost = SystemConfig.getConfig("mail_server_host");
//    private static int miPort = Integer.parseInt(SystemConfig.getConfig("mail_server_port"));
//    private static String mstrUser = SystemConfig.getConfig("mail_account");
//    private static String mstrEncryptPassword = SystemConfig.getConfig("mail_password");
//    private static String mstrProtocol = SystemConfig.getConfig("mail_protocol");
//    private static String mstrSender = SystemConfig.getConfig("mail_sender");
//    private static String mstrStartTLS = SystemConfig.getConfig("mail_server_start_TLS");
//    private static String mstrSSL = SystemConfig.getConfig("mail_server_SSL");

    //Test
    private String mstrHost = "smtp.gmail.com";
    //    private int miPort = 587;
    private int miPort = 465;
    private String mstrUser = "vinhdhq.dce.test@gmail.com";
    private String mstrEncryptPassword = "zgNKq/TukDSHNKMqL5RP+A==";
    private String mstrSender = "MBFSNM-DATASITE";
    private String mstrStartTLS = "1";
    private String mstrSSL = "1";

    private String mstrPasswordDecrypted = "";
    private boolean mbStartTLS;
    private boolean mbSSLOnConnect;


    public MailServicesUtil(String mstrHost, int miPort, String mstrUser, String mstrEncryptPassword, boolean mbStartTLS, boolean mbSSLOnConnect, String mstrSender) throws Exception {
        this.mstrHost = mstrHost;
        this.miPort = miPort;
        this.mstrUser = mstrUser;
        this.mstrEncryptPassword = mstrEncryptPassword;
        this.mbStartTLS = mbStartTLS;
        this.mbSSLOnConnect = mbSSLOnConnect;
        this.mstrSender = mstrSender;
//        EncryptionService encryptionService = new EncryptionService();
//        mstrPasswordDecrypted = encryptionService.decrypt(mstrEncryptPassword);
        mstrPasswordDecrypted = mstrEncryptPassword;
    }

    public MailServicesUtil() throws Exception {
//        EncryptionService encryptionService = new EncryptionService();
//        mstrPasswordDecrypted = encryptionService.decrypt(mstrEncryptPassword);
        mstrPasswordDecrypted = mstrEncryptPassword;
        mbStartTLS = mstrStartTLS.trim().equals("1");
        mbSSLOnConnect = mstrSSL.trim().equals("1");
    }

//    public void sendMailUsingJavaMail() {
//        Properties props = new Properties();
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.smtp.host", mstrHost);
//        props.put("mail.smtp.port", Integer.toString(miPort));
//
//        Session session = Session.getInstance(props,
//                new javax.mail.Authenticator() {
//                    protected PasswordAuthentication getPasswordAuthentication() {
//                        return new PasswordAuthentication(mstrUser, mstrPasswordDecrypted);
//                    }
//                });
//
//        try {
//
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(mstrUser));
//            message.setRecipients(Message.RecipientType.TO,
//                    InternetAddress.parse("vinhdhq@telsoft.com.vn"));
//            message.setSubject("Testing Subject");
//            message.setText("Dear Mail Crawler,"
//                    + "\n\n No spam to my email, please!");
//
//            Transport.send(message);
//
//            System.out.println("Done");
//
//        } catch (MessagingException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public void sendMailSmtps(List<String> listRecipient, String strSubject, String strBodyContent, List<String> listFileUrl) throws
            Exception {
        try {
            HtmlEmailSmtps email = new HtmlEmailSmtps();
            email.setSmtpPort(miPort);
            email.setHostName(mstrHost);
            email.setAuthentication(mstrUser, mstrPasswordDecrypted);
            email.setStartTLSEnabled(mbStartTLS);
            email.setSSLOnConnect(mbSSLOnConnect);
            email.setFrom(mstrUser, mstrSender);
            email.setCharset("UTF-8");
            email.setSubject(strSubject);
            email.setHtmlMsg(strBodyContent);
            if (listFileUrl != null) {
                for (String strFileUrl : listFileUrl) {
                    if (strFileUrl != null && !strFileUrl.trim().equals("")) {
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(strFileUrl);
                        attachment.setDisposition("attachment");
                        email.attach(attachment);
                    }
                }
            }
            if (listRecipient == null || listRecipient.size() <= 0) {
                return;
            }
            for (String strEmailAdd : listRecipient) {
                if (strEmailAdd != null && !strEmailAdd.trim().equals("")) {
                    email.addTo(strEmailAdd);
                }
            }
            email.send();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void processMail(Session mailsession, Transport transport, List<String> listEmail, String strMailSubject, String strMailBody, String mstrSender, List<String> listFile) throws Exception {
        try {
            // Create message
//            logMonitor("Sending mail: " + strMailSubject + "...");
            MimeMessage message = new MimeMessage(mailsession);
//            message.setHeader("Content-Type", "text/html; charset=UTF-8");
            // Set sender
            Address fromAdd = new InternetAddress(mstrSender);
            message.setFrom(fromAdd);
            // Set recipient
            for (String strRecipient : listEmail) {
                if (!strRecipient.equals("")) {
                    message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(strRecipient));
                }
            }
            message.setSubject(strMailSubject);

            // Create body
            Multipart multipart = new MimeMultipart("mixed");
            BodyPart bpContent = new MimeBodyPart();
            bpContent.setContent(strMailBody, "text/html; charset=UTF-8");
            multipart.addBodyPart(bpContent);
            if (listFile != null) {
                for (String htmlFileAttach : listFile) {
                    BodyPart bpContentAttach = new MimeBodyPart();
                    bpContentAttach.setContent(htmlFileAttach, "text/html; charset=UTF-8");
                    multipart.addBodyPart(bpContentAttach);
                }
            }
            message.setContent(multipart);
            // Send message
            transport.sendMessage(message, message.getAllRecipients());
//            logMonitor("...Done!");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
//            logMonitor("Error went sending email" + ex.getMessage());
        }
    }

    public void sendMail(List<String> listRecipient, String strSubject, String strBodyContent, List<String> listFileUrl) throws
            Exception {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setSmtpPort(miPort);
            email.setHostName(mstrHost);
            email.setAuthentication(mstrUser, mstrPasswordDecrypted);
            email.setStartTLSEnabled(mbStartTLS);
            email.setSSLOnConnect(mbSSLOnConnect);
            email.setFrom(mstrUser, mstrSender);
            email.setCharset("UTF-8");
            email.setSubject(strSubject);
            email.setMsg(strBodyContent);
            if (listFileUrl != null) {
                for (String strFileUrl : listFileUrl) {
                    if (strFileUrl != null && !strFileUrl.trim().equals("")) {
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(strFileUrl);
                        attachment.setDisposition("attachment");
                        email.attach(attachment);
                    }
                }
            }
            if (listRecipient == null || listRecipient.size() <= 0) {
                return;
            }
            for (String strEmailAdd : listRecipient) {
                if (strEmailAdd != null && !strEmailAdd.trim().equals("")) {
                    email.addTo(strEmailAdd);
                }
            }
            email.send();
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void sendMailHtml(List<String> listRecipient, String strSubject, String strBodyContent, String html, List<String> listFileUrl) throws
            Exception {
        try {
            HtmlEmail email = new HtmlEmail();
            email.setSmtpPort(miPort);
            email.setHostName(mstrHost);
            email.setAuthentication(mstrUser, mstrPasswordDecrypted);
            email.setStartTLSEnabled(mbStartTLS);
            email.setSSLOnConnect(mbSSLOnConnect);
            email.setFrom(mstrUser, mstrSender);
            email.setCharset("UTF-8");
            email.setSubject(strSubject);
            email.setHtmlMsg(strBodyContent + html);

            if (listFileUrl != null) {
                for (String strFileUrl : listFileUrl) {
                    if (strFileUrl != null && !strFileUrl.trim().equals("")) {
                        EmailAttachment attachment = new EmailAttachment();
                        attachment.setPath(strFileUrl);
                        attachment.setDisposition("attachment");
                        email.attach(attachment);
                    }
                }
            }
            if (listRecipient == null || listRecipient.size() <= 0) {
                return;
            }
            for (String strEmailAdd : listRecipient) {
                if (strEmailAdd != null && !strEmailAdd.trim().equals("")) {
                    email.addTo(strEmailAdd);
                }
            }
            email.send();
        } catch (Exception ex) {
            throw ex;
        }
    }


    protected int deleteFile(String strFileName) {
        File f1 = new File(strFileName);
        if (f1.exists() && f1.isFile()) {
            boolean bSuccess = f1.delete();
            if (bSuccess) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return -1;
        }
    }

    public boolean deleteFile(List<String> listFileName) {
        boolean bReturn = true;
        for (String strFileName : listFileName) {
            if (!strFileName.equals("")) {
                int iSuccess = deleteFile(strFileName);
                if (iSuccess == 0 || iSuccess == -1) {
                    bReturn = false;
                }
            }
        }
        return bReturn;
    }

    public List<String> zipFile(List<String> listFiles, String strZipFileName) throws Exception {
        List<String> vtReturn = new ArrayList<String>();
        if (!strZipFileName.endsWith(".zip")) {
            strZipFileName += ".zip";
        }
        try {
            if (listFiles.size() > 0) {
                String[] strListFile = listFiles.toArray(new String[listFiles.size()]);
                SmartZip.Zip(strListFile, strZipFileName, false);
                vtReturn.add(strZipFileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
        return vtReturn;
    }


    public static void main(String[] args) throws Exception {
        MailServicesUtil mailServicesUtil = new MailServicesUtil();

        String strContent = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">\n" +
                "<html><head><script type='text/javascript' src='includes/_shared/EnhanceJS/enhance.js'></script><script type='text/javascript' src='includes/newvi/js/excanvas.js'></script><script type='text/javascript' src='includes/_shared/jquery.min.js'></script><script type='text/javascript' src='includes/_shared/jqueryui.min.js'></script><script type='text/javascript' src='includes/newvi/js/visualize.jQuery.js'></script><script type='text/javascript' src='includes/newvi/js/example.js'></script><script type='text/javascript' src='includes/js/fg.menu.js'></script><script type='text/javascript' src='includes/js/timepicker.js'></script><script type='text/javascript' src='includes/js/jquery.fixedtableheader-1-0-2.min.js'></script><!-- CSS --><link rel='stylesheet' href='includes/newvi/css/visualize.css' type='text/css'><link rel='stylesheet' href='includes/newvi/css/visualize-light.css' type='text/css'><link rel='stylesheet' href='includes/css/style.css' type='text/css'><link rel='stylesheet' href='includes/css/fg.menu.css' type='text/css'><link rel='stylesheet' href='includes/css/smoothness/jquery-ui-1.7.2.custom.css' type='text/css'></head><body><span style='font-size: 16px;' >Hello day 01/03/2020</span><!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1 style=\"background-color:Tomato;\">Tomato</h1>\n" +
                "<h1 style=\"background-color:Orange;\">Orange</h1>\n" +
                "<h1 style=\"background-color:DodgerBlue;\"> hahahahahahahahahahah\n" +
                "Thử nghiệm gửi mail 01/03/2020</h1>\n" +
                "<h1 style=\"background-color:MediumSeaGreen;\">MediumSeaGreen</h1>\n" +
                "<h1 style=\"background-color:Gray;\">Gray</h1>\n" +
                "<h1 style=\"background-color:SlateBlue;\">SlateBlue</h1>\n" +
                "<h1 style=\"background-color:Violet;\">Violet</h1>\n" +
                "<h1 style=\"background-color:LightGray;\">LightGray</h1>\n" +
                "<table ><thead><tr Style='background:palegreen;font-weight: bold;' ><td Style='border:1px solid black;' ><span>LOG_ID</span></td><td Style='border:1px solid black;' ><span>ENTRY_DATE</span></td><td Style='border:1px solid black;' ><span>LOGGER</span></td><td Style='border:1px solid black;' ><span>LOG_LEVEL</span></td><td Style='border:1px solid black;' ><span>MESSAGE</span></td><td Style='border:1px solid black;' ><span>EXCEPTION</span></td></tr></thead><tbody><tr><td Style='border:1px solid black;' >e4a976c1-f554-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 12:20:56.724</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >TEST</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >e4afb852-f554-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 12:20:56.728</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >GetSampleCDRThreadManager is stopped</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >7f43e373-f55d-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:22:32.741</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >TEST</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >7f4b0f64-f55d-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:22:32.748</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >GetSampleCDRThreadManager is stopped</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >e70c1de5-f55f-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:39:45.853</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >TEST</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >e70ce136-f55f-11e9-aeb6-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:39:45.859</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >GetSampleCDRThreadManager is stopped</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >c3135381-f560-11e9-97d9-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:45:54.342</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >TEST</td><td Style='border:1px solid black;' > </td></tr><tr><td Style='border:1px solid black;' >c3143de2-f560-11e9-97d9-005056c0000b</td><td Style='border:1px solid black;' >2019-10-23 13:45:54.346</td><td Style='border:1px solid black;' ></td><td Style='border:1px solid black;' >INFO</td><td Style='border:1px solid black;' >GetSampleCDRThreadManager is stopped</td><td Style='border:1px solid black;' > </td></tr></tbody></table>\n" +
                "</body>\n" +
                "</html>\n" +
                "\n" +
                "Hello day 01/03/2020<h1>This is a Heading</h1>\n" +
                "<p>This is a paragraph.</p>\n" +
                "</body>\n" +
                "</body></html>";

        String strFileName = "excelReport/thử gửi file bù lu bù loa.txt";
        Charset utf8 = StandardCharsets.UTF_8;
        List<String> lstToWrite = Arrays.asList(strContent);
        Files.write(Paths.get(strFileName), lstToWrite, utf8);

//        mailServicesUtil.sendMail(Arrays.asList("ongsucu2005@gmail.com"), "Test Gửi mail Mail", strContent, Arrays.asList(strFileName));

        int i = 1;
    }

}
