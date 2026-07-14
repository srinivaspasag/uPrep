package com.lms.utils;

import com.lms.common.vedantu.enums.FollowType;

public interface ISocialEntity {
    String _getEntityId();

    void _setVoted(boolean voted);

    void _setFollowType(FollowType followType);
}
