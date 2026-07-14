package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.enums.FollowType;
import com.lms.pojos.ModuleEntryInfo;
import com.lms.pojos.ModuleSearchIndexDetails;
import com.lms.utils.ISocialEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetModuleRes extends ModuleSearchIndexDetails implements IListResponseObj, ISocialEntity {

    public boolean voted;
    public FollowType followType;
    public List<ModuleEntryInfo> moduleEntryInfos;


    @Override
    public void _setVoted(boolean voted) {
        this.voted = voted;
    }

    @Override
    public void _setFollowType(FollowType followType) {
        this.followType = followType;
    }

    @Override
    public String _getEntityId() {
        return id;
    }

}
