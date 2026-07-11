package com.vedantu.content.pojos.responses;

import java.util.List;

import com.vedantu.commons.enums.FollowType;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.utils.ISocialEntity;
import com.vedantu.content.pojos.ModuleEntryInfo;
import com.vedantu.content.search.details.ModuleSearchIndexDetails;


public class GetModuleRes extends ModuleSearchIndexDetails implements
IListResponseObj, ISocialEntity {

public boolean      voted;
public FollowType   followType;
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
