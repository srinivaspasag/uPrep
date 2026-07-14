package com.vedantu.organization.managers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.AbstractVedantuManager;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.fs.handlers.S3Handler;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.UniqueCodeUtils;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.models.Category;
import com.vedantu.organization.models.OrgCenter;
import com.vedantu.organization.models.OrgProgram;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.pojos.OrgProgramBasicInfo;
import com.vedantu.organization.pojos.OrgStructureBasicInfo;
import com.vedantu.organization.pojos.requests.organizations.ActivateOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.AddOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgReq;
import com.vedantu.organization.pojos.requests.organizations.GetOrgSectionInfoByAccessCodeReq;
import com.vedantu.organization.pojos.requests.organizations.OrgSectionAccessInfo;
import com.vedantu.organization.pojos.requests.organizations.RemoveOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateMaxDiscountReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSectionAccessReq;
import com.vedantu.organization.pojos.requests.organizations.UpdateOrgSectionReq;
import com.vedantu.organization.pojos.requests.organizations.UpdatePackageInfoReq;
import com.vedantu.organization.pojos.responses.organizations.ActivateOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.AddOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.CategoryInfo;
import com.vedantu.organization.pojos.responses.organizations.GetOrgSectionInfoByAccessCodeRes;
import com.vedantu.organization.pojos.responses.organizations.OrgSectionInfo;
import com.vedantu.organization.pojos.responses.organizations.RemoveOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateMaxDiscountRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgSectionAccessRes;
import com.vedantu.organization.pojos.responses.organizations.UpdateOrgSectionRes;
import com.vedantu.organization.pojos.responses.organizations.UpdatePackageInfoRes;
import com.vedantu.organization.pojos.utils.OrgStructureInfoNameComparator;

public class OrgSectionManager extends AbstractVedantuManager {

    private static final ALogger LOGGER = Logger.of(OrgSectionManager.class);

    public static AddOrgSectionRes addSection(AddOrgSectionReq addOrgSectionReq,
            boolean returnExisting) throws VedantuException {
        LOGGER.debug("request orgId" + addOrgSectionReq.orgId);
        if (addOrgSectionReq.thumbnail != null && !addOrgSectionReq.thumbnail.isEmpty()){
            addOrgSectionReq.thumbnail = getAWSFileUrl(addOrgSectionReq.imageNameWithExtension);
        }

        OrgSection orgSection = OrgSectionDAO.INSTANCE.addSection(addOrgSectionReq.orgId,
                addOrgSectionReq.code, addOrgSectionReq.name, addOrgSectionReq.desc,
                addOrgSectionReq.programId, addOrgSectionReq.centerId, returnExisting,
                addOrgSectionReq.descriptionPoints, addOrgSectionReq.thumbnail);

        AddOrgSectionRes addOrgSectionRes = new AddOrgSectionRes();
        addOrgSectionRes.id = orgSection._getStringId();
        addOrgSectionRes.recordState = orgSection.recordState;

        return addOrgSectionRes;
    }

    public static List<OrgSectionInfo> toOrgSectionInfo(List<OrgSection> sections) {

        List<OrgSectionInfo> sectionInfos = new ArrayList<OrgSectionInfo>();
        if (CollectionUtils.isNotEmpty(sections)) {
            for (OrgSection section : sections) {
                if (null == section) {
                    continue;
                }
                sectionInfos.add(toOrgSectionInfo(section));
            }
            Collections.sort(sectionInfos, OrgStructureInfoNameComparator.INSTANCE);
        }
        return sectionInfos;
    }

    public static List<CategoryInfo> toCategoryInfo(List<Category> categories) {

        List<CategoryInfo> categoryInfos = new ArrayList<CategoryInfo>();
        if (CollectionUtils.isNotEmpty(categories)) {
            for (Category category : categories) {
                if (null == category) {
                    continue;
                }
                CategoryInfo categoryInfo = new CategoryInfo(category._getStringId(),
                        category.getName(), category.sectionIds, category.description,
                        category.shortDescription, category.priority, category.banner,category.thumbnail);
                categoryInfos.add(categoryInfo);
            }
            // // Collections.sort(categoryInfos, OrgStructureInfoNameComparator.INSTANCE);
        }
        return categoryInfos;
    }

    public static UpdateOrgSectionRes updateSection(UpdateOrgSectionReq updateOrgSectionReq)
            throws VedantuException {
        if (updateOrgSectionReq.thumbnail != null && !updateOrgSectionReq.thumbnail.isEmpty()) {
            if (!updateOrgSectionReq.thumbnail.contains("https://s3.amazonaws.com/")) {
                updateOrgSectionReq.thumbnail = getAWSFileUrl(updateOrgSectionReq.imageNameWithExtension);
            }
        }

        OrgSection orgSection = OrgSectionDAO.INSTANCE.updateSection(updateOrgSectionReq.orgId,
                updateOrgSectionReq.sectionId, updateOrgSectionReq.code, updateOrgSectionReq.name,
                updateOrgSectionReq.programId, updateOrgSectionReq.accessScope,
                updateOrgSectionReq.revenueModel, updateOrgSectionReq.desc,
                updateOrgSectionReq.sdOnly, updateOrgSectionReq.descriptionPoints,
                updateOrgSectionReq.thumbnail);

        UpdateOrgSectionRes updateOrgSectionRes = new UpdateOrgSectionRes();
        updateOrgSectionRes.id = orgSection._getStringId();
        updateOrgSectionRes.recordState = orgSection.recordState;
        updateOrgSectionRes.edited = true;
        return updateOrgSectionRes;
    }

    public static UpdateOrgSectionAccessRes updateSectionAccess(
            UpdateOrgSectionAccessReq updateOrgSectionAccessReq) throws VedantuException {

        UpdateOrgSectionAccessRes updateOrgSectionAccessRes = new UpdateOrgSectionAccessRes();

        if (updateOrgSectionAccessReq.sectionAccessInfos == null) {
            throw new VedantuException(VedantuErrorCode.SECTION_LIST_NOT_SPECIFIED);
        }

        LOGGER.debug("......section array is not null.......");

        for (OrgSectionAccessInfo sectionAccessInfo : updateOrgSectionAccessReq.sectionAccessInfos) {
            sectionAccessInfo.validate();
        }

        LOGGER.debug("........section array is valid...........");

        for (OrgSectionAccessInfo sectionAccessInfo : updateOrgSectionAccessReq.sectionAccessInfos) {
            OrgSectionDAO.INSTANCE.updateSectionAccess(sectionAccessInfo.id,
                    sectionAccessInfo.accessScope, sectionAccessInfo.revenueModel,
                    sectionAccessInfo.costRate);
        }
        updateOrgSectionAccessRes.edited = true;
        return updateOrgSectionAccessRes;
    }

    public static RemoveOrgSectionRes removeSection(RemoveOrgSectionReq removeOrgSectionReq)
            throws VedantuException {

        OrgSection orgSection = OrgSectionDAO.INSTANCE.removeSection(removeOrgSectionReq.orgId,
                removeOrgSectionReq.sectionId);

        RemoveOrgSectionRes removeOrgSectionRes = new RemoveOrgSectionRes();
        removeOrgSectionRes.id = orgSection._getStringId();
        removeOrgSectionRes.recordState = orgSection.recordState;

        return removeOrgSectionRes;
    }

    public static ActivateOrgSectionRes
            activateSection(ActivateOrgSectionReq activateOrgSectionReq) throws VedantuException {

        OrgSection orgSection = OrgSectionDAO.INSTANCE.activateSection(activateOrgSectionReq.orgId,
                activateOrgSectionReq.sectionId);

        ActivateOrgSectionRes activateOrgSectionRes = new ActivateOrgSectionRes();
        activateOrgSectionRes.id = orgSection._getStringId();
        activateOrgSectionRes.recordState = orgSection.recordState;

        return activateOrgSectionRes;
    }

    public static GetOrgSectionInfoByAccessCodeRes getOrgSectionInfoByAccessCode(
            GetOrgSectionInfoByAccessCodeReq req) throws VedantuException {

        GetOrgSectionInfoByAccessCodeRes res = new GetOrgSectionInfoByAccessCodeRes();
        OrgSection orgSection = OrgSectionDAO.INSTANCE.getSectionByAccessCode(req.accessCode);
        res.section = (OrgStructureBasicInfo) orgSection.toBasicInfo();

        OrgProgram orgProgram = OrgProgramDAO.INSTANCE.getById(orgSection.programId);
        if (orgProgram == null) {
            LOGGER.error("orgProgram not found for the accessCode: " + req.accessCode);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_PROGRAM_NOT_FOUND);
        }

        res.program = (OrgProgramBasicInfo) orgProgram.toBasicInfo();

        GetOrgReq getOrgReq = new GetOrgReq();
        getOrgReq.orgId = orgSection.orgId;
        getOrgReq.getKey = req.getOrgKey;

        res.org = OrganizationManager.getOrganization(getOrgReq);

        OrgCenter orgCenter = OrgCenterDAO.INSTANCE.getById(orgSection.centerId);
        if (orgCenter == null) {
            LOGGER.error("orgCenter not found for the access: " + req.accessCode);
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_CENTER_NOT_FOUND);
        }

        res.center = (OrgStructureBasicInfo) orgCenter.toBasicInfo();

        return res;
    }

    // TODO: remove this code after deployment of version v.3.7.5
    public static int generateOrgSectionAccessCode() {

        List<OrgSection> orgSections = OrgSectionDAO.INSTANCE.find(
                OrgSectionDAO.INSTANCE.createQuery().filter("accessCode", null)).asList();
        for (OrgSection orgSection : orgSections) {
            orgSection.accessCode = UniqueCodeUtils.generateUniqueCode(EntityType.SECTION.name());
            OrgSectionDAO.INSTANCE.save(orgSection);
        }
        return orgSections.size();
    }

    public static List<OrgSection> getOrgSections(AccessScope scope,
            RevenueModel revenueModel) {

        List<OrgSection> orgSectionsList = new ArrayList<OrgSection>();
        orgSectionsList = OrgSectionDAO.INSTANCE.getOrgSections(scope, revenueModel);
        return orgSectionsList;

    }

    public static OrgSectionInfo toOrgSectionInfo(OrgSection section) {

        return new OrgSectionInfo(section._getStringId(), section.getName(), section.code,
                section.recordState, section.accessScope, section.revenueModel, section.desc,
                section.costRate,section.size,section.extSupported, section.orgId,
                section.startingRates, section.packagesMap, section.descriptionPoints,section.thumbnail);
    }

    public static UpdatePackageInfoRes updatePackageInfo(
            UpdatePackageInfoReq req) throws VedantuException {

        OrgSection orgSection = OrgSectionDAO.INSTANCE.updatePackageInfo(req.orgId,
                req.sectionId, req.packagesList);
        UpdatePackageInfoRes res = new UpdatePackageInfoRes();
        res.edited = true;
        return res;
    }

    public static UpdateMaxDiscountRes updateMaxDiscount(UpdateMaxDiscountReq req)
            throws VedantuException {
        // TODO Auto-generated method stub
        OrgSection orgSection = OrgSectionDAO.INSTANCE.updateMaxDiscount(req.sectionId,
                req.maxDiscount);
        UpdateMaxDiscountRes res = new UpdateMaxDiscountRes();
        res.edited = true;
        return res;
    }

    public static String getAWSFileUrl(String imageNameWithExtension) {
        LocalFileSystemHandler tempFs = FileSystemFactory.INSTANCE.getTempFS();
        String filePath = tempFs.getFilePath("organization", imageNameWithExtension);
        File file = new File(filePath);
        // move to s3 public bucket and get s3 url
        S3Handler handler = new S3Handler();
        try {
            handler.store(file, Play.application().configuration().getString("bucket.name"),
                    imageNameWithExtension, new HashMap<String, String>());
        } catch (FileStoreException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        FileUtils.deleteFile(imageNameWithExtension, file);
        String AWSFileUrl = "https://"+Play.application().configuration().getString("bucket.name")+".s3.amazonaws.com/"
                + imageNameWithExtension;
        return AWSFileUrl;
    }
}
