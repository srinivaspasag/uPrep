package com.lms.pojo.responce;

import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.pojo.OrgStructureBasicInfo;

import java.util.ArrayList;
import java.util.List;

public class GetCategorySectionRes implements IListResponseObj {
    public OrgStructureBasicInfo programInfo;
    public OrgStructureBasicInfo centerInfo;
    public OrgSectionInfo sectionInfo;

    public List<BoardBasicInfo> courseInfo = new ArrayList<BoardBasicInfo>();
    public boolean isPartOf = false;
    public boolean isB2C;
}
