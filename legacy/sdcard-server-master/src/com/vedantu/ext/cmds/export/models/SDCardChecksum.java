package com.vedantu.ext.cmds.export.models;

import org.json.JSONObject;

import com.vedantu.ext.cmds.web.JSONAware;

public class SDCardChecksum implements JSONAware {

    private String i;
    private String c;

    public SDCardChecksum(String info, String checkSum) {

        this.i = info;
        this.c = checkSum;
    }

  
    
    public String getI() {
    
        return i;
    }


    
    public void setI(String i) {
    
        this.i = i;
    }


    
    public String getC() {
    
        return c;
    }


    
    public void setC(String c) {
    
        this.c = c;
    }


    @Override
    public void fromJSON(JSONObject json) {

        // TODO Auto-generated method stub

    }

    @Override
    public JSONObject toJSON() {

        return new JSONObject(this);
    }

}
