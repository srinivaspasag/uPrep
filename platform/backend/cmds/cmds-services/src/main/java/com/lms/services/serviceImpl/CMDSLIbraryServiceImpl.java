package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.component.CMDSLIbraryMaanager;
import com.lms.managers.AbstractContentManager;
import com.lms.models.CMDSContentLink;
import com.lms.pojo.responce.ActionTakenRes;
import com.lms.pojos.requests.*;
import com.lms.pojos.responce.AddToLibraryRes;
import com.lms.pojos.responce.GetVisibilityChartRes;
import com.lms.pojos.responce.MakeVisibleRes;
import com.lms.services.CMDSLIbraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CMDSLIbraryServiceImpl extends AbstractContentManager implements CMDSLIbraryService {
    private static final Logger logger = LoggerFactory.getLogger(CMDSLIbraryServiceImpl.class);
    @Autowired
    private CMDSLIbraryMaanager cmdslIbraryMaanager;

    @Override
    public VedantuResponse addToLibrary(AddToLibraryReq addToLibraryReq) {
        AddToLibraryRes addToLibraryResponse = cmdslIbraryMaanager.addToLibrary(addToLibraryReq.userId,
                addToLibraryReq.orgId, addToLibraryReq.orgEntities, addToLibraryReq.contents);

        return new VedantuResponse(addToLibraryResponse);


    }

    @Override
    public VedantuResponse removeFromLibrary(AddToLibraryReq addToLibraryReq) {
        AddToLibraryRes addToLibraryResponse = cmdslIbraryMaanager.removeFromLibrary(addToLibraryReq.userId,
                addToLibraryReq.orgId, addToLibraryReq.orgEntities, addToLibraryReq.contents);

        return new VedantuResponse(addToLibraryResponse);

    }

    @Override
    public VedantuResponse getLibraryResources(GetLibraryResourcesReq getLibraryResourcesReq) {
        if (getLibraryResourcesReq.getOrgEntity() == null || getLibraryResourcesReq.getOrgEntity().getType() == null || getLibraryResourcesReq.getOrgEntity().getId() == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "missing orgEntityType and orgEntityId should not be null");

        }
        GetLibraryResourceRes getLibraryResponse = null;


        if (StringUtils.isEmpty(getLibraryResourcesReq.orderBy)
                || getLibraryResourcesReq.orderBy.equalsIgnoreCase("customOrder")) {

            getLibraryResourcesReq.orderBy = CMDSContentLink.POSITION;
            getLibraryResponse = cmdslIbraryMaanager.getResources2(getLibraryResourcesReq);
        } else {

            getLibraryResponse = cmdslIbraryMaanager.getResources(getLibraryResourcesReq);
        }


        return new VedantuResponse(getLibraryResponse);

    }

    @Override
    public VedantuResponse makeVisible(MakeVisibleReq makeVisibleReq) {
        logger.debug(" Called makeVisible");
        Map<String, Object> sessionParams = getReqParams();
        MakeVisibleRes response = cmdslIbraryMaanager.makeVisible(makeVisibleReq, sessionParams);
        return new VedantuResponse(response);


    }

    @Override
    public VedantuResponse getVisibilityStatus(GetVisibilityChartReq getVisibilityChartReq) {
        logger.debug(" Called content visibilty report");
        //
        if (getVisibilityChartReq.getContent() == null || getVisibilityChartReq.getContent().getId() == null || getVisibilityChartReq.getContent().getType() == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        GetVisibilityChartRes response = cmdslIbraryMaanager.getContentVisibilityReport(getVisibilityChartReq);
        //
        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse move(UpdateRankReq updateRankReq) {

        ActionTakenRes response = cmdslIbraryMaanager.updateLocation(updateRankReq);
        return new VedantuResponse(response);
    }

    protected Map<String, Object> getReqParams() {

        Map<String, String[]> reqParams = new HashMap<String, String[]>();
        //reqParams.putAll(request().queryString());

        Map<String, String[]> reqBodyParams = null;
        if (reqBodyParams != null) {
            reqParams.putAll(reqBodyParams);
        }

        Map<String, Object> allParams = new HashMap<String, Object>();
        if (null != reqParams && !reqParams.isEmpty()) {
            StringBuilder sb = new StringBuilder("reqParams : {");
            boolean isFirst = true;
            for (Map.Entry<String, String[]> entry : reqParams.entrySet()) {
                if (!isFirst) {
                    sb.append(", ");
                }
                //  sb.append(entry.getKey()).append("=[").append(entry.getValue().join(",")).append("]");
                isFirst = false;
            }
            sb.append("}");
            // Logger.log4j.info(sb.toString());
            for (Map.Entry<String, String[]> entry : reqParams.entrySet()) {
                List<String> value = null != entry.getValue() ? Arrays.asList(entry.getValue())
                        : null;
                if (null != value) {
                    allParams.put(entry.getKey(), value.size() == 1 ? value.get(0) : value);
                }

            }
        }
        return allParams;
    }
}
