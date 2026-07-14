package com.lms.models;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.pojo.BoardMappings;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(value = "boardMapping")
public class BoardMapping extends VedantuBaseMongoModel {

    public String orgId;
    public String userId;
    public String parentOrgId;
    public String sharedToOrgId;
    public boolean publish;
    public List<BoardMappings> boardMappings = new ArrayList<BoardMappings>();

    @Override
    public String toString() {
        return "BoardMapping [orgId=" + orgId + ", userId=" + userId + ", parentOrgId="
                + parentOrgId + ", sharedToOrgId=" + sharedToOrgId + ", boardMappings="
                + boardMappings + "]";
    }

}

