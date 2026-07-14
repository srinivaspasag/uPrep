package com.vedantu.eventbus.managers;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

import play.Play;

import com.vedantu.commons.content.interfaces.IEmailTemplateDetails;

public class NewMailManager {

    synchronized public static boolean sendEmail(IEmailTemplateDetails details) {

        HtmlEmail email = new HtmlEmail();
        try {
            email.setHostName(Play.application().configuration().getString("smtp.host"));
            //email.setSmtpPort(aPortNumber);
            email.setFrom("vikram.patil@vedantu.com");
            email.setTextMsg("testing email");
            email.addTo("vikram.patil@vedantu.com");
            email.send();
            return true;

        } catch (EmailException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;

    }
}
