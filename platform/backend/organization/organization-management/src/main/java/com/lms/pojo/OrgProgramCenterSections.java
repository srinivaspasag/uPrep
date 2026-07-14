package com.lms.pojo;

import com.lms.common.vedantu.constants.HardCodedConstants;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class OrgProgramCenterSections {

    public String centerId;
    public List<String> sectionIds;

    public OrgProgramCenterSections() {
        this(null);
    }

    public OrgProgramCenterSections(String centerId) {
        this.centerId = centerId;
        sectionIds = new ArrayList<String>();
    }

    public boolean hasSection(String sectionId) {
        return null != sectionIds && !sectionId.isEmpty()
                && sectionIds.contains(sectionId);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof OrgProgramCenterSections)) {
            return false;
        }
        OrgProgramCenterSections t = (OrgProgramCenterSections) o;
        return null != t && centerId.equals(t.getCenterId());
    }

    @Override
    public int hashCode() {
        return (centerId!=null)?centerId.hashCode():HardCodedConstants.emptyString.hashCode();

    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("OrgProgramCenterSections [centerId=");
        builder.append(centerId);
        builder.append(", sectionIds=");
        builder.append(sectionIds);
        builder.append("]");
        return builder.toString();
    }

}
