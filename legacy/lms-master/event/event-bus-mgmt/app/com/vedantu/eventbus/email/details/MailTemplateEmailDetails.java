package com.vedantu.eventbus.email.details;

import java.lang.reflect.Method;
import java.util.Date;

import play.Logger;
import play.Logger.ALogger;
import play.api.templates.Html;

import com.vedantu.eventbus.factory.EmailTemplateFactory;
import com.vedantu.user.pojos.UserEmailInfo;

public class MailTemplateEmailDetails extends AbstractEmailNotificationDetails {

    private final static ALogger LOGGER = Logger.of(MailTemplateEmailDetails.class);
    public String                title;
    public Html                  emailContent;
    public UserEmailInfo         user;

    public MailTemplateEmailDetails() {

        super(EmailTemplateFactory
                .getConfigurationKey(EmailTemplateFactory.TEMPLATE_CONFIGURATION_EMAIL_TEMPLATE));
    }

    @Override
    public String __getContent() {

        Html html = null;
        try {
            LOGGER.debug(" Email Rendering started at " + System.currentTimeMillis());

            LOGGER.debug(" Template class " + this.template);
            Class<?> templateInstance = this.__getTemplateClass();
            LOGGER.debug(" Template class " + templateInstance );
            Method render = templateInstance.getDeclaredMethod("render",this.getClass(), 
                    Html.class, UserEmailInfo.class);

            html = (Html) render.invoke(null, this, emailContent,user);

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

}
