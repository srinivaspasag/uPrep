package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.pojos.ModuleEntryCompletionRule;


public class UpdateModuleEntryReq extends AbstractAuthCheckReq{
    @Required
   public String moduleId;
   public int pos;
   public String name;
   public ModuleEntryCompletionRule completionRule;
}
    