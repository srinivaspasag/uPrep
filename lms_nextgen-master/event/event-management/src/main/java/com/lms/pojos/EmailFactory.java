package com.lms.pojos;

import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Value;

public class EmailFactory {
    private static EmailFactory instance = null;
    @Value("${smtp.host}")
    private String smtpHost;
    @Value("${smtp.port}")
    private int smtpPort = 25;
    private boolean isUsingTLS = false;
    @Value("${smtp.user}")
    private String smtpUserName = null;
    @Value("${smtp.password}")
    private String smtpUserPassword = null;

    private EmailFactory() {

        smtpPort = smtpPort;
        isUsingTLS = isUsingTLS;
        smtpHost = smtpHost;

        smtpUserName = smtpUserName;
        smtpUserPassword = smtpUserPassword;

    }

    private EmailFactory(String email, String password, String host) {

        smtpPort = smtpPort;
        isUsingTLS = isUsingTLS;
        smtpHost = host;//Play.application().configuration().getString("smtp.host", "127.0.0.1");

        smtpUserName = email;
        smtpUserPassword = password;

    }

    public static EmailFactory getInstance() {

        if (instance == null) {
            synchronized (EmailFactory.class) {
                if (instance == null) {
                    instance = new EmailFactory();

                }
            }

        }
        return instance;

    }

    public static EmailFactory getInstance(String email, String password, String host) {

        if (instance == null) {
            synchronized (EmailFactory.class) {
                if (instance == null) {
                    instance = new EmailFactory(email, password, host);

                }
            }

        } else {
            if (!instance.smtpUserName.equals(email) || !instance.smtpUserPassword.equals(password)) {
                synchronized (EmailFactory.class) {
                    instance = new EmailFactory(email, password, host);
                }
            }
        }
        return instance;

    }

    public HtmlEmail getEmail() {

        HtmlEmail email = new HtmlEmail();
        email.setHostName(smtpHost);
        email.setSmtpPort(smtpPort);
        if (isUsingTLS) {
            email.setStartTLSEnabled(isUsingTLS);
            email.setAuthentication(smtpUserName, smtpUserPassword);
        }

        return email;
    }

}
