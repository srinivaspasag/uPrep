package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetVisibilityChartReq extends AbstractAuthCheckReq {

    @NotBlank
    public SrcEntity content;
    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "start should not be null")
    public int start;
    public int size;

    public String validate() {

        if (content == null) {
            return "invalid content request";

        } else if ((!StringUtils.isEmpty(content.id) && content.type == EntityType.UNKNOWN)
                || content.type == null) {
            return "invalid content request";
        }
        return null;
    }
}
