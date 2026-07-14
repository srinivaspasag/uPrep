package com.vedantu.organization.daos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.google.code.morphia.query.UpdateResults;
import com.mongodb.MongoException.DuplicateKey;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.OrgProgramSectionBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;
import com.vedantu.organization.pojos.PackageInfo;

public class OrgSectionDAO extends VedantuBasicDAO<OrgSection, ObjectId> {

    private static final String       ADD_SIGN = "+";

    private static final ALogger      LOGGER   = Logger.of(OrgSectionDAO.class);

    public static final OrgSectionDAO INSTANCE = new OrgSectionDAO();

    private OrgSectionDAO() {

        super(OrgSection.class);
    }

    public List<OrgSection> getSectionsByIds(String orgId, String programId,
            List<ObjectId> sectionsIds) {

        return getSectionsByIds(orgId, programId, sectionsIds, null, null, null,
                MongoManager.NO_START, MongoManager.NO_LIMIT, new MutableLong());
    }

    public List<OrgSection> getSectionsByIds(String orgId, String programId,
            List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel,
            VedantuRecordState recordState, int start, int size, MutableLong totalHits) {
    	return getSectionsByIds(Arrays.asList(orgId),programId,sectionsIds,accessScope,revenueModel,recordState,start,size,totalHits);
    }

    public List<OrgSection> getSectionsByIds(List<String> orgIds, String programId,
            List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel,
            VedantuRecordState recordState, int start, int size, MutableLong totalHits) {

        LOGGER.debug("..........entered function getSectionsByIds......." + revenueModel);

        //Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
        Query<OrgSection> sectionsQuery = getQuery().field(FIELD_ID).hasAnyOf(orgIds);
        if (StringUtils.isNotEmpty(programId)) {
            sectionsQuery.filter("programId", programId);
        }

        // sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);

        if (CollectionUtils.isNotEmpty(sectionsIds)) {
            sectionsQuery.field(FIELD_ID).hasAnyOf(sectionsIds);
        }
        if (accessScope != null) {
            sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);
        }
        if (revenueModel != null) {
            sectionsQuery.filter(OrgSection.FIELD_REVENUE_MODEL, revenueModel);
        }

        if (recordState != null) {
            sectionsQuery.filter(ConstantsGlobal.RECORD_STATE, recordState);
        }
        sectionsQuery.order("cName");

        totalHits.setValue(sectionsQuery.countAll());
        List<OrgSection> sections = sectionsQuery.offset(start).limit(size).asList();
        LOGGER.debug("..........about to return from function getSectionsByIds......."
                + sections.size());
        return sections;
    }

    /**
     * Method used in {@link OrgMemberManager#getStudentsCount} for getting students count
     * written because original method was passing sectionids with variable name orgids
     * @param orgId
     * @param programId
     * @param sectionsIds
     * @param accessScope
     * @param revenueModel
     * @param recordState
     * @param start
     * @param size
     * @param totalHits
     * @return
     */
    public List<OrgSection> getSectionsById(String orgId, String programId,
            List<ObjectId> sectionsIds, AccessScope accessScope, RevenueModel revenueModel,
            VedantuRecordState recordState, int start, int size, MutableLong totalHits) {

        LOGGER.debug("..........entered function getSectionsByIds......." + revenueModel);

        Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
//        Query<OrgSection> sectionsQuery = getQuery().field(FIELD_ID).hasAnyOf(orgIds);
        if (StringUtils.isNotEmpty(programId)) {
            sectionsQuery.filter("programId", programId);
        }

        // sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);

        if (CollectionUtils.isNotEmpty(sectionsIds)) {
            sectionsQuery.field(FIELD_ID).hasAnyOf(sectionsIds);
        }
        if (accessScope != null) {
            sectionsQuery.filter(OrgSection.FIELD_ACCESS_SCOPE, accessScope);
        }
        if (revenueModel != null) {
            sectionsQuery.filter(OrgSection.FIELD_REVENUE_MODEL, revenueModel);
        }

        if (recordState != null) {
            sectionsQuery.filter(ConstantsGlobal.RECORD_STATE, recordState);
        }
        sectionsQuery.order("cName");

        totalHits.setValue(sectionsQuery.countAll());
        List<OrgSection> sections = sectionsQuery.offset(start).limit(size).asList();
        LOGGER.debug("..........about to return from function getSectionsByIds......."
                + sections.size());
        return sections;
    }

    public List<OrgSection> getSectionByCode(String orgId, Collection<String> codes) {

        Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
        if (CollectionUtils.isNotEmpty(codes)) {
            sectionsQuery.field(OrgSection.CODE).in(codes);
        }

        List<OrgSection> sections = sectionsQuery.asList();
        return sections;
    }

    public List<OrgSection> getSectionsByOrgIds(String orgId, String programId, String centerId) {

        Query<OrgSection> sectionsQuery = getQuery().filter("orgId", orgId);
        if (StringUtils.isNotEmpty(programId)) {
            sectionsQuery.filter("programId", programId);
        }
        if (StringUtils.isNotEmpty(centerId)) {
            sectionsQuery.filter("centerId", centerId);
        }
        sectionsQuery.order("cName");
        List<OrgSection> sections = sectionsQuery.asList();
        return sections;
    }

    public OrgSection addSection(String orgId, String code, String name, String desc,
            String programId, String centerId, boolean returnExisting, List<String> descriptionPoints, String thumbnail) throws VedantuException {

        LOGGER.debug("......entering addSection function......");
        OrgSection orgSection = getQuery().filter("orgId", orgId).filter("code", code)
                .order("cName").get();
        if (null != orgSection) {
            if (returnExisting) {
                LOGGER.debug("section already exists and will return the same for orgId: " + orgId
                        + ", code: " + code);
                return orgSection;
            }

            if (VedantuRecordState.ACTIVE == orgSection.recordState) {
                LOGGER.error("cannot add orgSection as orgSection already exists for orgId: "
                        + orgId + ", programId: " + programId + ", centerId: " + centerId
                        + ", code: " + code);
                throw new VedantuException(
                        VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_ALREADY_EXISTS,
                        "a section with code:" + code + " already exists for orgId:" + orgId);
            } else {
                LOGGER.error("changing orgSection recordState for orgId: " + orgId + ", code: "
                        + code + ", _id: " + orgSection._getStringId() + ", from: "
                        + orgSection.recordState + ", to: " + VedantuRecordState.ACTIVE);
                orgSection.setName(name);
                markActive(orgSection);
                save(orgSection);
                return orgSection;
            }
        }

        orgSection = new OrgSection(orgId, code, name, desc, programId, centerId, descriptionPoints, thumbnail);
        save(orgSection);

        return orgSection;
    }

    public OrgSection getSectionById(String orgId, String sectionId) throws VedantuException {

        OrgSection orgSection = getById(sectionId);
        if (null == orgSection) {
            LOGGER.error("cannot find orgSection for _id: " + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        if (!StringUtils.equals(orgSection.orgId, orgId)) {
            LOGGER.error("mismatch in orgId for section _id: " + sectionId + ", expected orgId: "
                    + orgSection.orgId + ", found orgId: " + orgId);
            throw new VedantuException(VedantuErrorCode.INVALID_ID);
        }
        return orgSection;
    }

    public OrgSection getSectionByAccessCode(String accessCode) throws VedantuException {

        OrgSection orgSection = createQuery().filter("accessCode", accessCode)
                .filter(RECORD_STATE, VedantuRecordState.ACTIVE).get();
        if (null == orgSection) {
            LOGGER.error("cannot find orgSection for accessCode: " + accessCode);
            throw new VedantuException(VedantuErrorCode.INVALID_ACCESS_CODE, "invalid access code");
        }

        return orgSection;
    }

    public OrgSection updateSection(String orgId, String sectionId, String code, String name,
            String programId, AccessScope accessScope, RevenueModel revenueModel, String desc,
            Boolean sdOnly,List<String> descriptionPoints,String thumbnail) throws VedantuException {
        OrgSection orgSection = getSectionById(orgId, sectionId);
        try {
            if (!StringUtils.equals(orgSection.programId, programId)) {
                LOGGER.error("mismatch in programId for section _id: " + sectionId
                        + ", expected programId: " + orgSection.programId + ", found programId: "
                        + programId);
                throw new VedantuException(VedantuErrorCode.INVALID_ID);
            }
            orgSection.code = code;
            orgSection.setName(name);
            if (accessScope != null) {
                orgSection.accessScope = accessScope;
            }
            if (revenueModel != null) {
                orgSection.revenueModel = revenueModel;
            }
            if (!StringUtils.isEmpty(desc)) {
                orgSection.desc = desc;
            }
            if (sdOnly != null) {
                orgSection.extSupported = sdOnly.booleanValue();
            }
            if(descriptionPoints != null && !descriptionPoints.isEmpty()){
                orgSection.descriptionPoints = descriptionPoints;
            }
            if(thumbnail != null && !thumbnail.isEmpty()){
                orgSection.thumbnail = thumbnail;
            }
            save(orgSection);

        } catch (DuplicateKey exception) {

            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_ALREADY_EXISTS);
        }
        return orgSection;
    }

    public OrgSection updateSectionAccess(String id, AccessScope accessScope,
            RevenueModel revenueModel, CostRate costRate) throws VedantuException {

        OrgSection orgSection = getById(id);
        if (orgSection == null) {
            return null;
        }
        orgSection.accessScope = accessScope;

        orgSection.revenueModel = revenueModel;

        orgSection.costRate = costRate;

        save(orgSection);

        return orgSection;
    }

    public OrgSection removeSection(String orgId, String sectionId) throws VedantuException {

        OrgSection orgSection = getSectionById(orgId, sectionId);

        markDeleted(orgSection);
        save(orgSection);
        return orgSection;
    }

    public OrgSection activateSection(String orgId, String sectionId) throws VedantuException {

        OrgSection orgSection = getSectionById(orgId, sectionId);
        try {
            markActive(orgSection);
            save(orgSection);
        } catch (DuplicateKey exception) {

            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_ALREADY_EXISTS);
        }
        return orgSection;
    }

    public List<OrgSection> getOrgSections(AccessScope scope, RevenueModel revenueModel) {

        List<OrgSection> sections = ds.find(OrgSection.class).field("scope").equal(scope)
                .field("revenueModel").equal(revenueModel).asList();
        return sections;
    }

    public Map<String, OrgStructureBasicInfo> getBasicInfosByIds(Collection<String> ids) {

        return getBasicInfosByIds(ids, false);
    }

    public Map<String, OrgStructureBasicInfo> getBasicInfosByIds(Collection<String> ids,
            boolean addSectionDetailInfo) {

        List<OrgSection> results = getByIds(ObjectIdUtils.toObjectIds(new ArrayList<String>(ids)));
        Map<String, OrgStructureBasicInfo> basicInfoMap = toBasicInfosMap(results);
        if (addSectionDetailInfo) {
            addSectionDetailInfo(results, basicInfoMap);
        }
        return basicInfoMap;
    }

    public void addSectionDetailInfo(List<OrgSection> sections,
            Map<String, OrgStructureBasicInfo> basicInfoMap) {

        for (OrgSection orgSection : sections) {
            OrgProgramSectionBasicInfo sectionBasicInfo = (OrgProgramSectionBasicInfo) basicInfoMap
                    .get(orgSection._getStringId());
            LOGGER.debug("OrgProgramSectionBasicInfo : " + sectionBasicInfo);
            if (sectionBasicInfo == null) {
                continue;
            }
            sectionBasicInfo.addSectionExtraInfo(orgSection);
        }
    }

    public static final String CENTER_SECTION_SEPARATOR = "/";

    public static String getCenterQualifiedSectionCode(String centerCode, String sectionCode) {

        return StringUtils.join(Arrays.asList(centerCode, sectionCode), CENTER_SECTION_SEPARATOR);
    }

    public static String getCenterPart(String centerQualifiedSectionCode) {

        String[] tokens = StringUtils.split(centerQualifiedSectionCode, CENTER_SECTION_SEPARATOR);
        return null != tokens && tokens.length == 2 ? tokens[0] : null;
    }

    public static String getSectionPart(String centerQualifiedSectionCode) {

        String[] tokens = StringUtils.split(centerQualifiedSectionCode, CENTER_SECTION_SEPARATOR);
        return null != tokens && tokens.length == 2 ? tokens[1] : null;
    }

    public Map<String, OrgStructureBasicInfo>
            getBasicInfosByCode(String orgId, String programId,
                    Map<String, OrgStructureBasicInfo> codeToCenter,
                    Set<String> centerQualifiedSectionCodes) throws VedantuException {

        Map<String, OrgStructureBasicInfo> centerQualifiedCodeToSection = new HashMap<String, OrgStructureBasicInfo>();

        for (String centerQualifiedCode : centerQualifiedSectionCodes) {
            if (StringUtils.isEmpty(centerQualifiedCode)) {
                continue;
            }
            String[] tokens = StringUtils.split(centerQualifiedCode, CENTER_SECTION_SEPARATOR);

            if (null == tokens || tokens.length != 2) {
                LOGGER.error("getBasicInfosByCode not properly qualified (center qualified) section: "
                        + centerQualifiedCode);
            }

            String centerCode = tokens[0];
            String sectionCode = tokens[1];

            OrgStructureBasicInfo centerBasicInfo = codeToCenter.get(centerCode);

            OrgSection section = getQuery().filter("orgId", orgId).filter("programId", programId)
                    .filter("centerId", centerBasicInfo.id).filter("code", sectionCode)
                    .order("cName").get();
            if (null == section) {
                LOGGER.error("getBasicInfosByCode no such (center qualified) section: "
                        + centerQualifiedCode);
                continue;
            }

            OrgStructureBasicInfo oSectionBasicInfo = (OrgStructureBasicInfo) section.toBasicInfo();

            centerQualifiedCodeToSection.put(centerQualifiedCode, oSectionBasicInfo);
        }

        return centerQualifiedCodeToSection;
    }

    public boolean addSize(List<String> ids, boolean remove, long size) {

        Query<OrgSection> sectionQuery = getQuery();
        sectionQuery = sectionQuery.field(FIELD_ID).in(ObjectIdUtils.toObjectIds(ids));
        String sign = ADD_SIGN;
        if (remove) {
            size = -size;
            sign = StringUtils.EMPTY;
        }

        sectionQuery = sectionQuery.where("this.size" + sign + size + ">=0");
        UpdateOperations<OrgSection> updateOperations = getDS().createUpdateOperations(
                OrgSection.class);
        LOGGER.debug("SectionQuery" + sectionQuery.toString() + " size " + size);

        updateOperations = updateOperations.inc(OrgSection.SIZE, size);

        UpdateResults<OrgSection> sectionUpdates = getDS().update(sectionQuery, updateOperations);

        if (sectionUpdates.getHadError()) {
            return false;
        }
        return true;
    }

    public boolean resetSize(List<String> ids, long size) {

        if (size < 0) {
            return false;
        }
        Query<OrgSection> sectionQuery = getQuery();
        sectionQuery = sectionQuery.field(FIELD_ID).in(ObjectIdUtils.toObjectIds(ids));

        UpdateOperations<OrgSection> updateOperations = getDS().createUpdateOperations(
                OrgSection.class);
        LOGGER.debug("SectionQuery" + sectionQuery.toString() + " size " + size);

        updateOperations = updateOperations.set(OrgSection.SIZE, size);

        UpdateResults<OrgSection> sectionUpdates = getDS().update(sectionQuery, updateOperations);

        if (sectionUpdates.getHadError()) {
            return false;
        }
        return true;
    }

    public OrgSection updatePackageInfo(String orgId, String sectionId,
            List<PackageInfo> packagesList) throws VedantuException {
//        OrgSection orgSection = getSectionById(orgId, sectionId);
        OrgSection orgSection = getById(sectionId);
        if (null == orgSection) {
            LOGGER.error("updatePackageInfo::cannot find orgSection for _id: " + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        Map<String, List<PackageInfo>> packagesMap = orgSection.packagesMap;
        if (packagesMap == null) {
            packagesMap = new HashMap<String, List<PackageInfo>>();
        }
        List<PackageInfo> packages = new ArrayList<PackageInfo>();
        for (PackageInfo packageInfo : packagesList) {
            if (packageInfo.numDays <= 0 || packageInfo.costRate.value <= 0) {
                throw new VedantuException(VedantuErrorCode.INVALID_PACKAGES_PRICING);
            }
            PackageInfo pInfo = new PackageInfo(packageInfo.numDays, packageInfo.costRate);
            packages.add(pInfo);
        }
        updateSavingsTxt(packages);
        Collections.sort(packages);
        if (!packages.isEmpty()) {
            packagesMap.put(orgId, packages);
        } else {
            if (packagesMap.containsKey(orgId)) {
                packagesMap.remove(orgId);
            }
        }
        orgSection.packagesMap = packagesMap;

        // Update startingRates by iterating over packages.
        Map<String, CostRate> startingRates = orgSection.startingRates;
        if (startingRates == null) {
            startingRates = new HashMap<String, CostRate>();
        }
        if (startingRates.containsKey(orgId)) {
            startingRates.remove(orgId);
        }
        if (!packages.isEmpty()) {
            CostRate minCostRate = packages.get(0).costRate;
            for (PackageInfo packageInfo : packages) {
                if (packageInfo.costRate.value < minCostRate.value) {
                    minCostRate = packageInfo.costRate;
                }
            }
            startingRates.put(orgId, minCostRate);
        }
        orgSection.startingRates = startingRates;
        save(orgSection);
        return orgSection;
    }

    public void updateSavingsTxt(List<PackageInfo> packages) {
        boolean monthlyPkgFound = false;
        CostRate monthlyRate = null;
        for (PackageInfo pkgInfo : packages) {
            if (pkgInfo.numDays == 30) {
                monthlyPkgFound = true;
                monthlyRate = pkgInfo.costRate;
                break;
            }
        }

        if (!monthlyPkgFound) return;
        for (PackageInfo pkgInfo : packages) {
            if (pkgInfo.numDays > 30) {
                double discountPercentage = calculateDiscountPercentage(pkgInfo, monthlyRate);
                if (discountPercentage >= 1.0) {
                    pkgInfo.savingsTxt = "SAVE " + Math.round(discountPercentage) + "%";
                }
            }
        }
    }

    private double calculateDiscountPercentage(PackageInfo pkgInfo, CostRate monthlyRate) {
        if (monthlyRate == null || monthlyRate.value == 0) return 0;
        int days = pkgInfo.numDays;
        int months = 0;
        if (days % 365 == 0) {
            months = (days / 365) * 12;
        } else if (days % 30 == 0) {
            months = (days / 30);
        }
        if (months == 0) return 0;

        int normalRate = months * monthlyRate.value;
        int actualRate = pkgInfo.costRate.value;
        if (actualRate >= normalRate) return 0;
        int discount = normalRate - actualRate;
        double discountPercentage = (discount * 100.0) / normalRate;
        return discountPercentage;
    }

    public OrgSection updateMaxDiscount(String sectionId, int maxDiscount)
            throws VedantuException {
        OrgSection orgSection = getById(sectionId);
        if (null == orgSection) {
            LOGGER.error("updateMaxDiscount::cannot find orgSection for _id: " + sectionId);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_SECTION_NOT_FOUND);
        }
        if (maxDiscount < 0 || maxDiscount >= 100) {
            throw new VedantuException(VedantuErrorCode.INVALID_INPUT_DATA);
        }
        orgSection.maxDiscount = maxDiscount;
        save(orgSection);
        return orgSection;
    }

    public OrgProgramBasicInfo getProgramFromSectionId(String id){

        OrgSection section = OrgSectionDAO.INSTANCE.getById(id);
        OrgProgram program = OrgProgramDAO.INSTANCE.getById(section.programId);
        OrgProgramBasicInfo programInfo = (OrgProgramBasicInfo) program.toBasicInfo();
        return programInfo;

    }

    public List<OrgSection> getOrganizationSections(String orgId) {
        Query<OrgSection> query = getQuery();
        query = query.field("orgId").equal(orgId);
        query = query.field("recordState").equal(VedantuRecordState.ACTIVE);
        // TODO Auto-generated method stub
        return query.asList();
    }

    public List<OrgSection> getSectionsByProgramId(String programId){
        Query<OrgSection> query = getQuery();
        query = query.filter("programId",programId);
        return query.asList();
    }
}
