package com.lms.components;

import com.lms.common.utils.FileUtils;
import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.Interval;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.entity.storage.IEntityFileStorage;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.entity.storage.UserProfilePicEntityFileStorage;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.ImageSize;
import com.lms.enums.OrgMemberProfile;
import com.lms.models.OrgMember;
import com.lms.pojo.OrgMemberExtendedInfo;
import com.lms.pojo.OrgMemberExtraInfo;
import com.lms.pojo.OrgMemberMappingExtendedInfo;
import com.lms.user.vedantu.user.enums.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class OrgMemberInfo {
    @Autowired
    private UserProfilePicEntityFileStorage userStorage;
    @Autowired
    private UserProfilePicEntityFileStorage userProfilePicEntityFileStorage;
    public ModelExtendedInfo toExtendedInfo(OrgMember orgMember) {

        OrgMemberMappingExtendedInfo tMappings = new OrgMemberMappingExtendedInfo();
        OrgMemberExtendedInfo extendedInfo = null;
        if (orgMember.interval == null) {
            orgMember.interval = new Interval(System.currentTimeMillis(), -1);
        }

        if (OrgMemberProfile.STUDENT == orgMember.profile) {
            extendedInfo = new OrgMemberExtendedInfo(orgMember._getStringId(), orgMember.recordState, orgMember.firstName,
                    orgMember.timeCreated, orgMember.lastUpdated, orgMember.userId, orgMember.orgId, orgMember.memberId, orgMember.firstName, orgMember.lastName, orgMember.dob,
                    orgMember.gender, orgMember.email, orgMember.profile,_getThumbnailUrl(orgMember), orgMember.contactNumber, tMappings, orgMember.father,
                    orgMember.mother, orgMember.guardian, orgMember.parentEmail,orgMember.status, orgMember.canImpersonate, orgMember.interval.getFrom(),
                    orgMember.interval.getTill());
        } else {
            extendedInfo = new OrgMemberExtendedInfo(orgMember._getStringId(), orgMember.recordState,orgMember.firstName,
                    orgMember.timeCreated, orgMember.lastUpdated, orgMember.userId, orgMember.orgId, orgMember.memberId, orgMember.firstName, orgMember.lastName, orgMember.dob,
                    orgMember.gender, orgMember.email, orgMember.profile, _getThumbnailUrl(orgMember), orgMember.contactNumber, tMappings,
                    orgMember.canImpersonate, orgMember.interval.getFrom(), orgMember.interval.getTill());

        }
        //extendedInfo.loginStatus = this.status;
        extendedInfo.extraInfo = orgMember.extraInfo == null ? new ArrayList<OrgMemberExtraInfo>()
                : orgMember.extraInfo;
        long currentTime = new Date().getTime();
        extendedInfo.userState =orgMember. _getMemberState(currentTime);
        return extendedInfo;
    }



    public String _getThumbnailUrl(OrgMember orgMember) {

        if (!StringUtils.isEmpty(orgMember.thumbnail)) {
            return getEntityThumbnail(EntityType.USER, orgMember.thumbnail);
        }

        List<String> suffixComponents = new ArrayList<String>();
        suffixComponents.add(orgMember.profile.name());

        if (OrgMemberProfile.STUDENT == orgMember.profile || OrgMemberProfile.TEACHER == orgMember.profile) {
            Gender tGender = (null != orgMember.gender && Gender.UNKNOWN != orgMember.gender) ? orgMember.gender : Gender.UNKNOWN;
            suffixComponents.add(tGender.name());
        }

        return ImageDisplayURLUtil.getEntityStaticThumbnail(EntityType.USER, suffixComponents);
    }

    public String getEntityThumbnail(EntityType entityType, String uid) {

        return getEntityImageURL(entityType, uid, ImageSize.SMALL);
    }

    public String getEntityImageURL(EntityType entityType, String uid, ImageSize size) {
        //  AbstractEntityFileStorage abstractEntityFileStorage = new AbstractEntityFileStorage();
        userProfilePicEntityFileStorage.AbstractEntityFileStorageEntity(entityType);
        IEntityFileStorage fileEntityStorage = userStorage;


        return ImageDisplayURLUtil.DEFAULT_FILE_SERVING_HOST_URL + fileEntityStorage.computeDisplayUrlComponent(uid, FileUtils.JPG_EXTENTION_WITHOUT_DOT, MediaType.IMAGE,
                FileCategory.CONVERTED, size);
    }
}
