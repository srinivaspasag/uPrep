package com.vedantu.organization.pojos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.models.OrgSection;

public class OrgProgramSectionBasicInfo extends OrgStructureBasicInfo {

    public Set<BoardBasicInfo> courses = new HashSet<BoardBasicInfo>();

    public AccessScope                    accessScope;
    public RevenueModel                   revenueModel;
    public String                         desc;
    public CostRate                       costRate;
    public long                           timeJoined;
    public long                           endTime;
    public String                         orderId;
    public long                           size;
    public boolean                        sdOnly;
    public String                         orgId;
    public Map<String, CostRate>          startingRates;
    public Map<String, List<PackageInfo>> packagesMap;
    public int                            maxDiscount;
    public List<String>                   descriptionPoints;
    public String                         thumbnail;

    public OrgProgramSectionBasicInfo(String id, VedantuRecordState recordState, String name,
            String code, EntityType type) {

        super(id, recordState, name, code, type);
        this.desc = StringUtils.EMPTY;
    }

    private Map<String, BoardBasicInfo> map = new HashMap<String, BoardBasicInfo>();

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

}
