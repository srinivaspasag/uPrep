package com.lms.pojos.requests;

import com.lms.api.IRequestParamsValidator;
import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.Scope;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class PublishQuestionAsChallengeReq extends PublishQuestionReq implements
        IRequestParamsValidator {

    @NotBlank(message = "channelId should not be null")
    public String channelId;

    @NotBlank(message = "name should not be null")
    public String name;
    public Scope scope;
    public int lifeTime;
    public int duration;
    public int maxBid;
    @NotBlank(message = "name should not be null")
    public String publishType;
    public String difficulty;
    public int initialBidPool;
    public List<Integer> hintsDeduction;

    @Override
    public boolean validateRequestParams() throws VedantuException {

        if ((lifeTime < duration) || maxBid == 0) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }
        return false;
    }
}
