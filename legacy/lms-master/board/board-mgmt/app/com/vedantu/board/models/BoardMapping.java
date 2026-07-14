package com.vedantu.board.models;

import java.util.ArrayList;
import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.board.pojos.BoardMappings;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "boardMapping", noClassnameStored = true)
public class BoardMapping extends VedantuBaseMongoModel{

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
