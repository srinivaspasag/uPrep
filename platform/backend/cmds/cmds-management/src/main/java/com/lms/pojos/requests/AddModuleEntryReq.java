package com.lms.pojos.requests;

import com.lms.models.ModuleEntry;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class AddModuleEntryReq extends AbstractOrgScopeReq {

    @NotBlank(message = "children should not be null")
    public List<ModuleEntry> children;
    @NotBlank(message = "moduleId should not be null")
    public String moduleId;
    @NotBlank(message = "pos should not be null")
    public int pos;

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