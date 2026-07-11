package com.vedantu.cmds.pojos.responses.library;

import java.util.List;

import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class VisibilityReport implements IListResponseObj {

    public OrgStructureBasicInfo programInfo;
    public OrgStructureBasicInfo centerInfo;
    public OrgStructureBasicInfo sectionInfo;
    public Scope                 visibility;
    public boolean               downloadable;
    public List<SrcEntity>        downloadableEntities;
}
