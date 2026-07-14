package com.vedantu.ext.cmds.export.models;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;

public class SDCardMetadata implements JSONAware {

    /**
     * 
     */

    private String name;
    private String id;
    private long   size;
    private long   contentSize=0;

    public SDCardMetadata(String name, String id, long size,long contentSize) {

        super();
        this.name = name;
        this.id = id;
        this.size = size;
        this.contentSize=contentSize;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public long getSize() {

        return size;
    }

    public void setSize(long size) {

        this.size = size;
    }

    
    public long getContentSize() {
    
        return contentSize;
    }

    
    public void setContentSize(long contentSize) {
    
        this.contentSize = contentSize;
    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

    @Override
    public void fromJSON(JSONObject json) {

        // TODO Auto-generated method stub

    }

}
