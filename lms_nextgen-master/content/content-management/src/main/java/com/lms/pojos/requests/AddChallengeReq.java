package com.lms.pojos.requests;

import com.lms.api.IRequestParamsValidator;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.Scope;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class AddChallengeReq extends AbstractAddContentReq implements IRequestParamsValidator {

    @NotBlank(message = "channelId should be required")
    public String channelId;
    @NotBlank(message = "name should be required")
    public String name;
    public int lifeTime;
    public int duration;
    public int maxBid;
    public Scope publishType;
    public String difficulty;
    @NotBlank(message = "qid should be required")
    public String qid;
    public Scope scope;
    public List<String> hints;
    public int initialBidPool;
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
