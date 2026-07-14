package com.vedantu.content.pojos.requests.challenges;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.Scope;
import com.vedantu.content.apis.IRequestParamsValidator;
import com.vedantu.content.pojos.requests.AbstractAddContentReq;

public class AddChallengeReq extends AbstractAddContentReq implements IRequestParamsValidator {

    @Required
    public String        channelId;
    @Required
    public String        name;
    public int           lifeTime;
    public int           duration;
    public int           maxBid;
    public Scope         publishType;
    public String        difficulty;
    @Required
    public String        qid;
    public Scope         scope;
    public List<String>  hints;
    public int           initialBidPool;
    public List<Integer> hintsDeduction;

    @Override
    public boolean validateRequestParams() throws VedantuException {

        if (contentSrc == null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, "contentSrc is missing");
        }
        if (lifeTime < duration || maxBid == 0) {
            throw new VedantuException(VedantuErrorCode.INVALID_LIFE_TIME, "Lifetime[" + lifeTime
                    + "] should be > Duration[" + duration + "] and Maximum points[" + maxBid
                    + "] should be > 0 ");
        }
        if (CollectionUtils.isNotEmpty(hints)) {
            if (hintsDeduction == null || hintsDeduction.size() != hints.size()) {
                throw new VedantuException(VedantuErrorCode.MISSING_POINT_DEDUCTION_VALUE,
                        "missing point deduction for hints : " + hints + " , pointDeducation: "
                                + hintsDeduction);
            }

            int previousDeducation = 0;
            for (Integer hintDeducation : hintsDeduction) {
                if (hintDeducation > maxBid
                        || (previousDeducation != 0 && previousDeducation > hintDeducation)) {
                    throw new VedantuException(VedantuErrorCode.INVALID_POINT_DEDUCTION_VALUE,
                            "invalid hint diduction value[" + hintDeducation
                                    + "], previousHintDeducation value[" + previousDeducation + "]");
                }
                previousDeducation = hintDeducation.intValue();
            }
        }
        return true;
    }
}
