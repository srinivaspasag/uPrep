package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.CampaignCode;
import com.lms.models.SalesCampaign;
import com.lms.pojo.request.AddCampaignCodeReq;
import com.lms.pojo.request.ApplyCampaignCodeReq;
import com.lms.pojo.request.CreateBulkCampaignCodeReq;
import com.lms.pojo.request.GetCampaignCodeReq;
import com.lms.pojo.responce.*;
import com.lms.repository.CampaignCodeRepo;
import com.lms.repository.SalesCampaignRepo;
import com.lms.service.CampaignCodesServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

@Service
public class CampaignCodesServicesImpl implements CampaignCodesServices {
    private static final Logger logger = LoggerFactory.getLogger(CampaignCodesServicesImpl.class);

    @Autowired
    private CampaignCodeRepo campaignCodeRepo;
    @Autowired
    private SalesCampaignRepo salesCampaignRepo;
    static final String SOURCE = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz";
    static SecureRandom secureRnd = new SecureRandom();


    @Override
    public VedantuResponse addCampainCode(AddCampaignCodeReq addCampaignCodeReq) {
        if (addCampaignCodeReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AddCampaignCodeRes addCampaignCodeRes = addCampaignCode(addCampaignCodeReq);

        return new VedantuResponse(addCampaignCodeRes);
    }

    @Override
    public VedantuResponse createBulkCampainCodes(CreateBulkCampaignCodeReq createBulkCampaignCodeReq) {

        if (createBulkCampaignCodeReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        CreateBulkCampaignCodeRes createBulkCampaignCodeRes = createBulkCampaignCodes(createBulkCampaignCodeReq);

        return new VedantuResponse(createBulkCampaignCodeRes);

    }

    @Override
    public VedantuResponse getCampainCode(GetCampaignCodeReq getCampaignCodeReq) {
        if (getCampaignCodeReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetCampaignCodeRes getCampaignCodeRes = getCampaignCode(getCampaignCodeReq);

        return new VedantuResponse(getCampaignCodeRes);
    }

    @Override
    public VedantuResponse isValidCampainCode(GetCampaignCodeReq getCampaignCodeReq) {
        if (getCampaignCodeReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ValidateCampaignCodeRes validateCampaignCodeRes = isValidCampaignCode(getCampaignCodeReq);

        return new VedantuResponse(validateCampaignCodeRes);
    }

    @Override
    public VedantuResponse applyCampainCode(ApplyCampaignCodeReq applyCampaignCodeReq) {
        if (applyCampaignCodeReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        ApplyCampaignCodeRes applyCampaignCodeRes = applyCampaignCode(applyCampaignCodeReq);

        return new VedantuResponse(applyCampaignCodeRes);
    }

    private ApplyCampaignCodeRes applyCampaignCode(ApplyCampaignCodeReq request) {


            ApplyCampaignCodeRes response = new ApplyCampaignCodeRes();
            CampaignCode campaignCode = getCampaignCodeByCode(request.campaignCode);
            if (campaignCode == null) {
                logger.error("campaignCode does not exist, check the code : " + request.campaignCode);
                throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
            }
            if (campaignCode.maxUsageCount <= campaignCode.currentUsageCount) {
                logger.error("campaignCode used maximum times : " + request.campaignCode);
                response.applied = false;
                response.message = "Code is used maximum times";
                return response;
            }
            SalesCampaign salesCampaign = salesCampaignRepo.findByIdAndRecordState(campaignCode.getSalesCampaignId(),VedantuRecordState.ACTIVE);
            if (salesCampaign.startTime > System.currentTimeMillis()
                    || salesCampaign.expiryTime < System.currentTimeMillis()) {
                logger.error("salesCampaign is not started or expired " + campaignCode.salesCampaignId);
                response.applied = false;
                response.message = "Campaign not started or expired";
                return response;
            }
            campaignCode.currentUsageCount += 1;
            campaignCode.consumerUserIds.add(request.userId);
            if (campaignCode.maxUsageCount == campaignCode.currentUsageCount) {
                campaignCode.expired = true;
            }
            campaignCodeRepo.save(campaignCode);
            response.applied = true;
            response.message = "Applied Successfully";
            return response;

    }

    private ValidateCampaignCodeRes isValidCampaignCode(GetCampaignCodeReq request) {

        ValidateCampaignCodeRes response = new ValidateCampaignCodeRes();
        CampaignCode campaignCode = getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            logger.error("campaignCode does not exist, check the code : " + request.campaignCode);
            response.campaignCodeExists = false;
            return response;
        }
        SalesCampaign salesCampaign = salesCampaignRepo.findByIdAndRecordState(campaignCode.getSalesCampaignId(),VedantuRecordState.ACTIVE);
        if (salesCampaign.startTime > System.currentTimeMillis()
                || salesCampaign.expiryTime < System.currentTimeMillis()) {
            logger.error("salesCampaign is not started or expired " + campaignCode.salesCampaignId);
            response.campaignCodeExists = false;
            return response;
        }
        response.campaignCodeExists = true;
        return response;

    }

    private GetCampaignCodeRes getCampaignCode(GetCampaignCodeReq request) {
        GetCampaignCodeRes response = new GetCampaignCodeRes();
        CampaignCode campaignCode = getCampaignCodeByCode(request.campaignCode);
        if (campaignCode == null) {
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        response.campaignCode = campaignCode;
        return response;
    }

    private CreateBulkCampaignCodeRes createBulkCampaignCodes(CreateBulkCampaignCodeReq request) {

        CreateBulkCampaignCodeRes response = new CreateBulkCampaignCodeRes();
        for (int i = 0; i < request.numberOfCampaignCodesRequired; i++) {
            CampaignCode campaignCode = new CampaignCode();
            campaignCode.salesCampaignId = request.salesCampaignId;
            campaignCode.code = generateCampaignCode();
            campaignCode.expired = false;
            campaignCode.maxUsageCount = 1;
            campaignCodeRepo.save(campaignCode);
            response.campaignCodes.add(campaignCode);
        }
        response.success = true;
        return response;
    }

    private AddCampaignCodeRes addCampaignCode(AddCampaignCodeReq request) {
        AddCampaignCodeRes response = new AddCampaignCodeRes();
        CampaignCode campaignCode = new CampaignCode(request.salesCampaignId, request.maxUsageCount);
        campaignCode.code = generateCampaignCode();
        campaignCode.expired = false;
        campaignCodeRepo.save(campaignCode);
        response.done = true;
        response.campaignCode = campaignCode;
        return response;
    }
    public String generateCampaignCode() {
        String campaignCode = randomString(8).toLowerCase();
        boolean isUniqueCampaignCode = checkCampaignCodeUniqueness(campaignCode);
        if (!isUniqueCampaignCode) {
            campaignCode = generateCampaignCode();
        }
        return campaignCode;
    }

    public boolean checkCampaignCodeUniqueness(String campaignCode) {

        List<CampaignCode> campaignCodes = campaignCodeRepo.findByCode(campaignCode);
        if (campaignCode==null||campaignCodes.isEmpty()) {
            return true;
        }
        return false;
    }

    public static String randomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(SOURCE.charAt(secureRnd.nextInt(SOURCE.length())));
        return sb.toString();
    }
    public CampaignCode getCampaignCodeByCode(String code) {

        CampaignCode campaignCode = campaignCodeRepo.findByCodeAndRecordState(code, VedantuRecordState.ACTIVE);

        return campaignCode;
    }

}
