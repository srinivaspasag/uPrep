package com.vedantu.cmds.pojos.requests.slpmodules;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.content.models.ModuleEntry;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class AddModuleEntryReq extends AbstractOrgScopeReq {

    @Required
    public List<ModuleEntry> children;
    @Required
    public String            moduleId;
    @Required
    public int               pos;

    @Override
    public String validate() {

        String superValidate = super.validate();
        if (null != superValidate) {
            return superValidate;
        }
        int i = 0;
        for (ModuleEntry moduleEntry : children) {
            if (StringUtils.isEmpty(moduleEntry.name) && moduleEntry.entity == null) {
                return "both name and entity can not be null at index: " + i;
            }
            i++;
        }

        return null;
    }
}