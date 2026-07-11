package com.lms.pojo;

import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.CostRate;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.RevenueModel;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.OrgSection;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Setter
@Getter
public class OrgProgramSectionBasicInfo extends OrgStructureBasicInfo {

    public Set<BoardBasicInfo> courses = new HashSet<BoardBasicInfo>();

    public AccessScope accessScope;
    public RevenueModel revenueModel;
    public String desc;
    public CostRate costRate;
    public long timeJoined;
    public long endTime;
    public String orderId;
    public long size;
    public boolean sdOnly;
    public String orgId;
    public Map<String, CostRate> startingRates;
    public Map<String, List<PackageInfo>> packagesMap;
    public int maxDiscount;
    public List<String> descriptionPoints;
    public String thumbnail;

    public OrgProgramSectionBasicInfo(String id, VedantuRecordState recordState, String name,
                                      String code, EntityType type) {

        super(id, recordState, name, code, type);
        this.desc = "";
    }

    private final Map<String, BoardBasicInfo> map = new HashMap<String, BoardBasicInfo>();

    public BoardBasicInfo _getOrAddProgramCourse(BoardBasicInfo o) {

        if (!map.containsKey(o.id)) {
            map.put(o.id, o);
            courses.add(o);
        }
        return map.get(o.id);
    }

    /**
     * these fields will only be populated on demand
     */

    public void addSectionExtraInfo(OrgSection orgSection) {

        this.accessScope = orgSection.accessScope;
        this.revenueModel = orgSection.revenueModel;
        this.desc = orgSection.desc;
        this.costRate = orgSection.costRate;
        this.size = orgSection.size;
        this.sdOnly = orgSection.extSupported;
        this.orgId = orgSection.orgId;
        this.startingRates = orgSection.startingRates;
        this.packagesMap = orgSection.packagesMap;
        this.maxDiscount = orgSection.maxDiscount;
        this.descriptionPoints = orgSection.descriptionPoints;
        this.thumbnail = orgSection.thumbnail;
    }

    public OrgProgramSectionBasicInfo(OrgSection orgSection) {

        super(orgSection.id.toString(), orgSection.recordState, orgSection.getName(), orgSection.code, EntityType.SECTION);
        this.desc = "";
        this.accessScope = orgSection.accessScope;
        this.revenueModel = orgSection.revenueModel;
        this.desc = orgSection.desc;
        this.costRate = orgSection.costRate;
        this.size = orgSection.size;
        this.sdOnly = orgSection.extSupported;
        this.orgId = orgSection.orgId;
        this.startingRates = orgSection.startingRates;
        this.packagesMap = orgSection.packagesMap;
        this.maxDiscount = orgSection.maxDiscount;
        this.descriptionPoints = orgSection.descriptionPoints;
        this.thumbnail = orgSection.thumbnail;
    }

}
