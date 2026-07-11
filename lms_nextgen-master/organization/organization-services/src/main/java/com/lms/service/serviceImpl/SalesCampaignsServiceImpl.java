package com.lms.service.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.models.SalesCampaign;
import com.lms.pojo.request.AddSalesCampaignReq;
import com.lms.pojo.request.DeleteSalesCampaignReq;
import com.lms.pojo.request.GetSalesCampaignReq;
import com.lms.pojo.request.UpdateSalesCampaignReq;
import com.lms.pojo.responce.AddSalesCampaignRes;
import com.lms.pojo.responce.DeleteSalesCampaignRes;
import com.lms.pojo.responce.UpdateSalesCampaignRes;
import com.lms.repository.SalesCampaignRepo;
import com.lms.service.SalesCampaignsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalesCampaignsServiceImpl implements SalesCampaignsService {
    private static final Logger logger = LoggerFactory.getLogger(LicensingServiceImpl.class);
    @Autowired
    private SalesCampaignRepo salesCampaignRepo;
    @Override
    public VedantuResponse addSaleCampaign(AddSalesCampaignReq addSalesCampaignReq) {

        if (addSalesCampaignReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        AddSalesCampaignRes addSalesCampaignRes = addSalesCampaign(addSalesCampaignReq);

        return new VedantuResponse(addSalesCampaignRes);
    }

    @Override
    public VedantuResponse getSaleCampaign(GetSalesCampaignReq getSalesCampaignReq) {
        if (getSalesCampaignReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        SalesCampaign getSalesCampaignRes = getSalesCampaign(getSalesCampaignReq);

        return new VedantuResponse(getSalesCampaignRes);
    }

    @Override
    public VedantuResponse getSaleCampaigns(GetSalesCampaignReq getSalesCampaignReq) {
        if (getSalesCampaignReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        List<SalesCampaign> getSalesCampaignsRes = getSalesCampaigns(getSalesCampaignReq);

        return new VedantuResponse(getSalesCampaignsRes);
    }

    @Override
    public VedantuResponse updateSaleCampaign(UpdateSalesCampaignReq updateSalesCampaignReq) {
        if (updateSalesCampaignReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        UpdateSalesCampaignRes updateSalesCampaignRes = updateSalesCampaign(updateSalesCampaignReq);

        return new VedantuResponse(updateSalesCampaignRes);
    }

    @Override
    public VedantuResponse deleteSalesCampaign(DeleteSalesCampaignReq deleteSalesCampaignReq) {
        if (deleteSalesCampaignReq==null) {
            throw  new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        DeleteSalesCampaignRes deleteSalesCampaignRes = deleteSaleCampaign(deleteSalesCampaignReq);

        return new VedantuResponse(deleteSalesCampaignRes);
    }

    private DeleteSalesCampaignRes deleteSaleCampaign(DeleteSalesCampaignReq request) {
        DeleteSalesCampaignRes response = new DeleteSalesCampaignRes();
        SalesCampaign salesCampaign = salesCampaignRepo.findByIdAndRecordState(request.salesCampaignId,VedantuRecordState.ACTIVE);
        if (salesCampaign == null) {
            logger.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        salesCampaign.recordState = VedantuRecordState.DELETED;
        salesCampaignRepo.save(salesCampaign);
        response.salesCampaign = salesCampaign;
        return response;
    }


    private UpdateSalesCampaignRes updateSalesCampaign(UpdateSalesCampaignReq request) {
        UpdateSalesCampaignRes response = new UpdateSalesCampaignRes();
        Optional<SalesCampaign> salesCampaign = salesCampaignRepo.findById(request.salesCampaignId);
        if (!salesCampaign.isPresent()) {
            logger.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        salesCampaign.get().setName(request.name);
        salesCampaign.get().setRewardType(request.rewardType);
        salesCampaign.get().setRewardValue(request.rewardValue);
        salesCampaign.get().setStartTime(request.startTime);
        salesCampaign.get().setExpiryTime(request.expiryTime);
        salesCampaign.get().isActive=request.isActive;
        salesCampaignRepo.save(salesCampaign.get());
        response.salesCampaign = salesCampaign.get();
        return response;
    }

    private List<SalesCampaign> getSalesCampaigns(GetSalesCampaignReq getSalesCampaignReq) {
        List<SalesCampaign> salesCampaigns = salesCampaignRepo.findAllByRecordState(VedantuRecordState.ACTIVE);
        if (salesCampaigns==null||salesCampaigns.isEmpty()) {
            return null;
        }
        return salesCampaigns;
    }

    private SalesCampaign getSalesCampaign(GetSalesCampaignReq request) {
        Optional<SalesCampaign> salesCampaign = salesCampaignRepo.findById(request.salesCampaignId);
        if (!salesCampaign.isPresent()) {
            logger.error("salesCampaign does not exist, check the id : " + request.salesCampaignId);
            throw new VedantuException(VedantuErrorCode.DOES_NOT_EXIST);
        }
        return salesCampaign.get();
    }

    private AddSalesCampaignRes addSalesCampaign(AddSalesCampaignReq request) {
        AddSalesCampaignRes response = new AddSalesCampaignRes();
        SalesCampaign salesCampaign = new SalesCampaign(request.name, request.rewardType,
                request.rewardValue, request.startTime, request.expiryTime, request.isActive);
        salesCampaignRepo.save(salesCampaign);
        response.done = true;
        response.salesCampaign = salesCampaign;
        return response;
    }
}
