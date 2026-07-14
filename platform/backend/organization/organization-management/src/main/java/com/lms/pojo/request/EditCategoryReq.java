package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.Set;


@Getter
@Setter
public class EditCategoryReq extends AbstractOrgScopeReq {
    @NotBlank(message = "id should not be null")
    public String id;
    public String name;
    public Set<String> sectionIds;
}
