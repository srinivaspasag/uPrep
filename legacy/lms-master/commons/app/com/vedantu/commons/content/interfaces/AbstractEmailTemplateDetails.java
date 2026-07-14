package com.vedantu.commons.content.interfaces;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.api.templates.Html;

import com.vedantu.commons.constants.Configurations;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public abstract class AbstractEmailTemplateDetails implements IEmailTemplateDetails, IEventDetails {

    public static class UserEmailPojos implements JSONAware {

        public static final String NAME  = "name";
        public static final String EMAIL = "email";
        public String              name;
        public String              email;

        public UserEmailPojos() {

        }

        public UserEmailPojos(String name, String recepient) {

            this.name = name;
            this.email = recepient;
        }

        @Override
        public JSONObject toJSON() throws JSONException {

            JSONObject json = new JSONObject();
            json.put(NAME, name);
            json.put(EMAIL, email);
            return json;

        }

        @Override
        public void fromJSON(JSONObject json) {

            name = JSONUtils.getString(json, NAME);
            email = JSONUtils.getString(json, EMAIL);

        }

        @Override
        public String toString() {

            return "UserEmailPojos [name=" + name + ", email=" + email + "]";
        }

        @Override
        public UserEmailPojos clone() {

            UserEmailPojos pojo = new UserEmailPojos();
            pojo.name = this.name;
            pojo.email = this.email;
            return pojo;
        }

    };

    public static final String     CLAZZ             = "clazz";
    public static final String     SUBJECT           = "subject";
    public static final String     SENDER            = "sender";
    public static final String     RECEIVER_EMAILS   = "receiverEmails";
    public static final String     CC_EMAILS         = "ccEmails";
    public static final String     BCC_EMAILS        = "bccEmails";
    private static final String    VIEWS_HTML_EMAILS = "views.html.emails";

    private final static ALogger   LOGGER            = Logger.of(AbstractEmailTemplateDetails.class);

    final protected String         template;

    protected List<UserEmailPojos> receiverEmails;
    protected List<UserEmailPojos> ccEmails;
    protected List<UserEmailPojos> bccEmails;

    protected UserEmailPojos       sender;

    protected String               subject;

    protected String               clazz;

    protected Map<String, String>  emailHeaders;

    public String                  appDomain;

    public String                  appProtocol;

    protected AbstractEmailTemplateDetails(final String configurationName) {

        template = Play.application().configuration().getString(configurationName);

        String senderEmail = Play.application().configuration()
                .getString(EmailConfigurationConstants.SENDER);
        sender = new UserEmailPojos("Learnpedia", senderEmail);
        receiverEmails = new ArrayList<UserEmailPojos>();
        ccEmails = new ArrayList<UserEmailPojos>();
        bccEmails = new ArrayList<UserEmailPojos>();
        emailHeaders = new HashMap<String, String>();
        appDomain = Play.application().configuration().getString(Configurations.APPLEARN_HOST);
        appProtocol = Play.application().configuration().getString(Configurations.APP_PROTOCOL);
    }

    protected AbstractEmailTemplateDetails(AbstractEmailTemplateDetails details) {

        this.template = details.template;
        receiverEmails = new ArrayList<UserEmailPojos>();
        ccEmails = new ArrayList<UserEmailPojos>();
        bccEmails = new ArrayList<UserEmailPojos>();
        emailHeaders = new HashMap<String, String>();
        appDomain = Play.application().configuration().getString(Configurations.APPLEARN_HOST);
        appProtocol = Play.application().configuration().getString(Configurations.APP_PROTOCOL);

        for (UserEmailPojos userEmailPojo : details.receiverEmails) {
            this.receiverEmails.add(userEmailPojo.clone());

        }
        for (UserEmailPojos userEmailPojo : details.ccEmails) {
            this.ccEmails.add(userEmailPojo.clone());

        }

        for (UserEmailPojos userEmailPojo : details.bccEmails) {
            this.bccEmails.add(userEmailPojo.clone());

        }

        this.sender = details.sender.clone();
        this.subject = details.subject;
        this.clazz = details.clazz;
        this.appDomain = details.appDomain;
        this.appProtocol = details.appProtocol;
        for (String key : emailHeaders.keySet()) {
            this.emailHeaders.put(key, this.emailHeaders.get(key));
        }

    }

    public String getAppDomain() {

        return appDomain;
    }

    public String getAppProtocol() {

        return appProtocol;
    }

    @Override
    public String __getContent() {

        Html html = null;
        try {
            LOGGER.debug(" Email Rendering started at " + System.currentTimeMillis());

            LOGGER.debug(" Template class " + this.template);
            Class<?> templateInstance = this.__getTemplateClass();
            LOGGER.debug(" Template class " + templateInstance + this.template);
            Method render = templateInstance.getDeclaredMethod("render", this.getClass());

            html = (Html) render.invoke(null, this);

            LOGGER.debug(" Email Rendered " + new Date());

        } catch (SecurityException e) {
            LOGGER.debug(" Can not get declared method found  in generated template types ", e);
        } catch (NoSuchMethodException e) {

            LOGGER.debug(" No render method found  in generated template types ", e);
        } catch (Exception e) {

            LOGGER.debug(" Failed while rendering html template ", e);
        }
        if (html == null) {
            return null;
        }
        return html.body();
    }

    @Override
    public Class<?> __getTemplateClass() throws ClassNotFoundException {

        return Class.forName(VIEWS_HTML_EMAILS + "." + template);
    }

    public boolean addRecepient(String name, String recepient) {

        LOGGER.debug("Adding receiver" + name + "  email " + recepient);
        return receiverEmails.add(new UserEmailPojos(name, recepient));
    }

    public boolean setSubject(String subject) {

        this.subject = subject;
        return true;
    }

    public boolean addBccRecepient(String name, String recepient) {

        return bccEmails.add(new UserEmailPojos(name, recepient));
    }

    public boolean addCCRecepient(String name, String recepient) {

        return bccEmails.add(new UserEmailPojos(name, recepient));
    }

    @Override
    public List<UserEmailPojos> getRecepients() {

        return receiverEmails;
    }

    @Override
    public List<UserEmailPojos> getCCRecepients() {

        return ccEmails;
    }

    @Override
    public List<UserEmailPojos> getBCCRecepients() {

        return bccEmails;
    }

    @Override
    public UserEmailPojos getSender() {

        return sender;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        JSONUtils.addJSONAwareObjectList(RECEIVER_EMAILS, receiverEmails, json);
        // if (CollectionUtils.isNotEmpty(receiverEmails)) {
        // json.put(RECEIVER_EMAILS, receiverEmails);
        // }
        JSONUtils.addJSONAwareObjectList(BCC_EMAILS, bccEmails, json);
        // if (CollectionUtils.isNotEmpty(bccEmails)) {
        // json.put(BCC_EMAILS, bccEmails);
        // }

        JSONUtils.addJSONAwareObjectList(CC_EMAILS, ccEmails, json);
        // if (CollectionUtils.isNotEmpty(ccEmails)) {
        // json.put(CC_EMAILS, ccEmails);
        // }
        json.put(SENDER, sender.toJSON());
        json.put(SUBJECT, subject);
        json.put(CLAZZ, this.getClass().getName());
        json.put("emailHeaders", emailHeaders);
        return json;

    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromJSON(JSONObject json) {

        receiverEmails = (List<UserEmailPojos>) JSONUtils.getJSONAwareCollection(
                UserEmailPojos.class, json, RECEIVER_EMAILS);
        bccEmails = (List<UserEmailPojos>) JSONUtils.getJSONAwareCollection(UserEmailPojos.class,
                json, BCC_EMAILS);
        ccEmails = (List<UserEmailPojos>) JSONUtils.getJSONAwareCollection(UserEmailPojos.class,
                json, CC_EMAILS);

        sender = (UserEmailPojos) JSONUtils.getJSONAware(new UserEmailPojos(), json, SENDER);
        subject = JSONUtils.getString(json, SUBJECT);
        clazz = JSONUtils.getString(json, CLAZZ);
        JSONObject emailHeadersObject = JSONUtils.getJSONObject(json, "emailHeaders");
        emailHeaders = new HashMap<String, String>();
        if (emailHeadersObject != null) {
            Iterator<?> keyIter = emailHeadersObject.keys();
            while (keyIter.hasNext()) {
                String key = (String) keyIter.next();
                try {
                    emailHeaders.put(key, (String) emailHeadersObject.get(key));
                } catch (JSONException e) {
                    LOGGER.debug(" Json for", e);
                }

            }
        }
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }

    @Override
    public Map<String, String> getHeaders() {

        return emailHeaders;
    }

    public boolean addHeader(String header, String field) {

        if (emailHeaders == null) {
            emailHeaders = new HashMap<String, String>();
        }

        return emailHeaders.put(header, field) != null;
    }

    public void resetRecepients() {

        this.bccEmails.clear();
        this.ccEmails.clear();
        this.receiverEmails.clear();
    }

}
