package com.lms.pojo.request;

import com.lms.pojo.responce.AbstractOrgStructureRes;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UpdateOrgSectionRes extends AbstractOrgStructureRes {
    public boolean edited;
}
