package com.vedantu.cmds.pojos.export;

import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.user.pojos.UserInfo;

public class ExportRecordInfo extends ModelExtendedInfo {

    public ModelBasicInfo  sectionInfo;
    public UserInfo        userInfo;
    public UserInfo        exportedFor;
    public ModelBasicInfo  programInfo;
    public ModelBasicInfo  centerInfo;
    public ModelBasicInfo  orgInfo;

    public EncryptionLevel encLevel;

    public ExportRecordInfo(String id, VedantuRecordState recordState, String name,
            long timeCreated, long lastUpdated) {

        super(id, recordState, name, timeCreated, lastUpdated);
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString() {

        return "ExportRecordInfo [sectionInfo=" + sectionInfo + ", orgMemberInfo=" + userInfo
                + ", exportedFor=" + exportedFor + ", toString()=" + super.toString() + "]";
    }

}
