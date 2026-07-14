package com.vedantu.organization.pojos.responses.organizations;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;

public class GetCategorySectionRes implements IListResponseObj {

    public OrgStructureBasicInfo programInfo;
    public OrgStructureBasicInfo centerInfo;
    public OrgSectionInfo        sectionInfo;

    public List<BoardBasicInfo>  courseInfo = new ArrayList<BoardBasicInfo>();
    public boolean               isPartOf = false;
    public boolean               isB2C;
}
