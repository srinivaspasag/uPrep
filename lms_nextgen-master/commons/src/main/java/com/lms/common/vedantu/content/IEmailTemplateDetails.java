package com.lms.common.vedantu.content;


import java.util.List;
import java.util.Map;

public interface IEmailTemplateDetails {

    String getSubject();

    List<AbstractEmailTemplateDetails.UserEmailPojos> getRecepients();

    List<AbstractEmailTemplateDetails.UserEmailPojos> getCCRecepients();

    List<AbstractEmailTemplateDetails.UserEmailPojos> getBCCRecepients();

    Class<?> __getTemplateClass() throws ClassNotFoundException;

    AbstractEmailTemplateDetails.UserEmailPojos getSender();

    boolean verify();

    Map<String, String> getHeaders();

    String __getContent();

}
