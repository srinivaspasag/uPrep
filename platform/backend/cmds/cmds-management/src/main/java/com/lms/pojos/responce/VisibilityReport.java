package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.Scope;
import com.lms.pojo.OrgStructureBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class VisibilityReport implements IListResponseObj {

    public OrgStructureBasicInfo programInfo;
    public OrgStructureBasicInfo centerInfo;
    public OrgStructureBasicInfo sectionInfo;
    public Scope visibility;
    public boolean downloadable;
    public List<SrcEntity> downloadableEntities;
}
