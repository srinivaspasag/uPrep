package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.content.CMDSFolderInfo;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Setter
@Getter
@Document(value = "cmdsfolders")
@CompoundIndexes(@CompoundIndex(name = "parent,organizationId,name", unique = true))
public class CMDSFolder extends AbstractBoardEntityTagModel {


    public String organizationId;
    public String parent;
    public List<String> parentSources;

    public boolean isRoot;


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
                true, true, null, recordState);

        return folderInfo;

    }

    @Override
    public String toString() {

        return "CMDSFolder [organizationId=" + organizationId + ", parent=" + parent
                + ", parentSources=" + parentSources + ", isRoot=" + isRoot + ",id="
                + _getStringId() + "]";
    }

}
