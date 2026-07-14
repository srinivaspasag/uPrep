package com.lms.pojos;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class OrgDetails {
    public String orgName;
    public String orgId;
    public boolean publishStatus;
    public List<SharedBoardInfo> sharedBoards = new ArrayList<SharedBoardInfo>();
    public List<String> unSharedBoards = new ArrayList<String>();
}
