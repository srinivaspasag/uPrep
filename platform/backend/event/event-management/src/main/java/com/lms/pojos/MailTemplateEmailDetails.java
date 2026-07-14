package com.lms.pojos;

import com.lms.user.vedantu.user.pojo.UserEmailInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailTemplateEmailDetails extends AbstractEmailNotificationDetails {

    private final static Logger logger = LoggerFactory.getLogger(MailTemplateEmailDetails.class);
    public String title;
    // public Html                  emailContent;
    public UserEmailInfo user;

    public MailTemplateEmailDetails() {

        super(EmailTemplateFactory.getConfigurationKey(EmailTemplateFactory.TEMPLATE_CONFIGURATION_EMAIL_TEMPLATE));
    }

    @Override
    public String __getContent() {

       /* Html html = null;
        try {
            logger.debug(" Email Rendering started at " + System.currentTimeMillis());

            logger.debug(" Template class " + this.template);
            Class<?> templateInstance = this.__getTemplateClass();
            logger.debug(" Template class " + templateInstance );
            Method render = templateInstance.getDeclaredMethod("render",this.getClass(),
                    Html.class, UserEmailInfo.class);

            html = (Html) render.invoke(null, this, emailContent,user);

            logger.debug(" Email Rendered " + new Date());

        } catch (SecurityException e) {
            logger.debug(" Can not get declared method found  in generated template types ", e);
        } catch (NoSuchMethodException e) {

            logger.debug(" No render method found  in generated template types ", e);
        } catch (Exception e) {

            logger.debug(" Failed while rendering html template ", e);
        }
        if (html == null) {
            return null;
        }
        return html.body();*/
        return null;
    }

}
