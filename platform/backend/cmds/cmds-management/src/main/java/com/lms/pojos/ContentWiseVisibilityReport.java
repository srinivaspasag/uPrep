package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.pojo.OrgStructureBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class ContentWiseVisibilityReport implements IListResponseObj {
    public SrcEntity content;
    public OrgStructureBasicInfo sectionInfo;
    public Scope visibility;
    public String errorCode;
    public Boolean downloadable;
    public List<SrcEntity> downloadableEntities;
}
