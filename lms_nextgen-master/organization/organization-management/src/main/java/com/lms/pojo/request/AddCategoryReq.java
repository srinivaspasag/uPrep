package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Set;


@Getter
@Setter
public class AddCategoryReq extends AbstractOrgScopeReq {

    @NotBlank(message = "name should not be null")
    public String name;

    public Set<String> sectionIds;

}
