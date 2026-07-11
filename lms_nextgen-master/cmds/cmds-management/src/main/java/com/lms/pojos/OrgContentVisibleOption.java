package com.lms.pojos;

import com.lms.common.pojos.ScheduleInfo;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Setter
@Getter
public class OrgContentVisibleOption {
    private static final Logger logger = LoggerFactory.getLogger(OrgContentVisibleOption.class);
    public SrcEntity orgEntity;
    public ScheduleInfo schedule;
    public Boolean visible;
    public Boolean downloadble;
    public EncryptionLevel encLevel;
    public List<SrcEntity> downloadableEntities;  //only for module

    public String validate() {
        logger.debug(".....Inside option validate function.........");
        if (orgEntity == null) {
            logger.debug(".....org entity missing........");
            return "org entity missing ";
        }
       /* String value = orgEntity.validate();
        if (value != null) {
            logger.debug(".....org entity not found........");
            return "org entity not found";

        }*/

        if (orgEntity.type != EntityType.SECTION) {
            logger.debug(".....invalid orgEntity........");
            return "invalid orgEntity";
        }
        if (schedule == null && visible == null && downloadble == null) {
            logger.debug(".....invalid visible option........");
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
