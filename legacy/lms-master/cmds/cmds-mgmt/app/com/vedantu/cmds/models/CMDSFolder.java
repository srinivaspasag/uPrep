package com.vedantu.cmds.models;

import java.util.List;

import play.Logger;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.cmds.pojos.content.question.CMDSFolderInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.models.AbstractBoardEntityTagModel;

@Entity(value = "cmdsfolders", noClassnameStored = true)
@Indexes(@Index(value="parent,organizationId,name",unique=true))
public class CMDSFolder extends AbstractBoardEntityTagModel {

    public String       organizationId;
    public String       parent;
    public List<String> parentSources;
    public boolean      isRoot;
    

    public CMDSFolder() {

    }

    public CMDSFolder(String userId, String name, String organizationid, String parent) {

        isRoot = false;
        this.userId = userId;
        this.name = name;
        this.organizationId = organizationid;
        this.parent = parent;
        this.contentType = EntityType.FOLDER;
    }

  
    public ModelBasicInfo toBasicInfo() {

        CMDSFolderInfo folderInfo = new CMDSFolderInfo(this._getStringId(), name,
                EntityType.FOLDER, organizationId, timeCreated, lastUpdated, userId, 0, false,
              true, true,  null, recordState);
        Logger.debug("Decorating folder Info " + this._getStringId());

        return folderInfo;

    }

    @Override
    public String toString() {

        return "CMDSFolder [organizationId=" + organizationId + ", parent=" + parent
                + ", parentSources=" + parentSources + ", isRoot=" + isRoot + ",id="
                + _getStringId() + "]";
    }

}
