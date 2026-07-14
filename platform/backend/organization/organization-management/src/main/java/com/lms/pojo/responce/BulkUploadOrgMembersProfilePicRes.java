package com.lms.pojo.responce;

import java.util.HashMap;
import java.util.Map;


public class BulkUploadOrgMembersProfilePicRes {

    public Map<String, UploadOrgPicRes> status;

    public BulkUploadOrgMembersProfilePicRes() {
        this.status = new HashMap<String, UploadOrgPicRes>();
    }

}
