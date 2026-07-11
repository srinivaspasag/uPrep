package com.vedantu.commons.content.interfaces;

import java.util.List;
import java.util.Map;

import com.vedantu.commons.content.interfaces.AbstractEmailTemplateDetails.UserEmailPojos;

public interface IEmailTemplateDetails {

    public String getSubject();

    public List<UserEmailPojos> getRecepients();

    public List<UserEmailPojos> getCCRecepients();

    public List<UserEmailPojos> getBCCRecepients();

    public Class<?> __getTemplateClass() throws ClassNotFoundException;

    public UserEmailPojos getSender();

    public boolean verify();

    public Map<String, String> getHeaders();

    public String __getContent();

}
