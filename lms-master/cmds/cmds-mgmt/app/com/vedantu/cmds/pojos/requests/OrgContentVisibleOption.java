package com.vedantu.cmds.pojos.requests;

import java.util.List;

import play.Logger;

import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;

public class OrgContentVisibleOption {

    public SrcEntity       orgEntity;
    public ScheduleInfo    schedule;
    public Boolean         visible;
    public Boolean         downloadble;
    public EncryptionLevel encLevel;
    public List<SrcEntity> downloadableEntities;  //only for module

    public String validate() {
        Logger.debug(".....Inside option validate function.........");
        if (orgEntity == null) {
            Logger.debug(".....org entity missing........");
            return "org entity missing ";
        }
        String value = orgEntity.validate();
        if (value != null) {
            Logger.debug(".....org entity not found........");
            return "org entity not found";

        }

        if (orgEntity.type != EntityType.SECTION) {
            Logger.debug(".....invalid orgEntity........");
            return "invalid orgEntity";
        }
        if (schedule == null && visible == null && downloadble == null) {
            Logger.debug(".....invalid visible option........");
            return "invalid visible option";
        }

        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("OrgEntity:").append(orgEntity).append("\nscheduleInfo").append(schedule)
                .append("\nvisible:").append(visible).append("\ndownloadable:").append(downloadble);
        return builder.toString();
    }
}
