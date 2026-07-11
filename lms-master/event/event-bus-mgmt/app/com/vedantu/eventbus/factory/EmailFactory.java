package com.vedantu.eventbus.factory;

import org.apache.commons.mail.HtmlEmail;

import play.Play;

public class EmailFactory {

    private String       smtpHost         = null;
    private int          smtpPort         = 25;
    private boolean      isUsingTLS       = false;

    private String       smtpUserName     = null;
    private String       smtpUserPassword = null;

    private static EmailFactory instance         = null;

    private EmailFactory() {

        smtpPort = Play.application().configuration().getInt("smtp.port", 25);
        isUsingTLS = Play.application().configuration().getBoolean("smtp.tls",false);
        smtpHost = Play.application().configuration().getString("smtp.host", "127.0.0.1");

        smtpUserName = Play.application().configuration().getString("smtp.user");
        smtpUserPassword = Play.application().configuration().getString("smtp.password");

    }

    private EmailFactory(String email, String password, String host) {

        smtpPort = Play.application().configuration().getInt("smtp.port", 25);
        isUsingTLS = Play.application().configuration().getBoolean("smtp.tls",false);
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

    public static EmailFactory getInstance(String email,String password, String host) {

        if (instance == null) {
            synchronized (EmailFactory.class) {
                if (instance == null) {
                    instance = new EmailFactory(email,password,host);

                }
            }

        }else{
            if(!instance.smtpUserName.equals(email) || !instance.smtpUserPassword.equals(password)){
                synchronized (EmailFactory.class) {
                    instance = new EmailFactory(email,password,host);
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
            email.setTLS(isUsingTLS);
            email.setAuthentication(smtpUserName, smtpUserPassword);
        }

        return email;
    }

}
