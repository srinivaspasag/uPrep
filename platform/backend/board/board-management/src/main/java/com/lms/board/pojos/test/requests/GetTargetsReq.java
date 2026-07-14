package com.lms.board.pojos.test.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetTargetsReq extends AbstractAuthCheckReq {
    @NotBlank
    public String ownerId;

    public String validate() {
        String superValidate = super.validate();
        if (!StringUtils.isEmpty(superValidate)) {
            return superValidate;
        }
        if (StringUtils.isEmpty(ownerId)) {
            return "ownerId missing";
        }
        return null;
    }

}
