package com.lms.pojo.responce;

import com.lms.board.model.GranteeOrgProgram;
import com.lms.common.vedantu.commons.pojos.requests.responces.ListResponse;
import com.lms.pojo.OrgBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class GetSharedOrgsRes  extends ListResponse<GranteeOrgProgram> {
    public long totalHits;
    public List<OrgBasicInfo> subscriberOrgsInfos = new ArrayList<OrgBasicInfo>();
    public Map<String, String> orgsKeyValue = new HashMap<String,String>();

    public GetSharedOrgsRes(){

    }
}
