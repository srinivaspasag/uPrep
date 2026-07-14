package com.vedantu.cmds.pojos;

import java.util.ArrayList;
import java.util.List;

public class OrgDetails {
    public String orgName;
    public String orgId;
    public boolean publishStatus;
    public List<SharedBoardInfo> sharedBoards = new ArrayList<SharedBoardInfo>();
    public List<String> unSharedBoards = new ArrayList<String>();
}
