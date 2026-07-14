package com.vedantu.ext.cmds.pojo.responses;

import org.json.JSONObject;

public class GetFileInfosRes extends ListResponse<DownloadableFileInfo> {

    public GetFileInfosRes() {

        super(DownloadableFileInfo.class);
        // TODO Auto-generated constructor stub
    }

    @Override
    public void fromJSON(JSONObject json) {

        super.fromJSON(json);

    }

    @Override
    public JSONObject toJSON() {

        // TODO Auto-generated method stub
        return null;
    }

}
