package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
@Setter
@Getter
public class GetContentReq {
    @NotBlank
    public String       orgId;
    public String       userId;
    public String       programId;
    public String       parentId;
    public List<String> brdIds;
    public Integer      start;
    public Integer      size;
    public boolean      keepModuleResult;
}
