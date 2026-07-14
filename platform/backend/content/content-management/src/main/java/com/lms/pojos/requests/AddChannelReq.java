package com.lms.pojos.requests;

import com.lms.common.vedantu.enums.Scope;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddChannelReq extends AbstractOrgScopeReq
{
    @NotBlank(message = "name should not be empty")
    public String name;
    public Scope scope;

    @Override
    public String toString() {
        return " [name=" + name + ", scope=" + scope + ", toString()="
                + super.toString() + "]";
    }
}
