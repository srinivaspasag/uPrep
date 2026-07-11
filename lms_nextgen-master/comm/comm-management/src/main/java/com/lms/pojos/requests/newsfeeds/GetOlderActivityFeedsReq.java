package com.lms.pojos.requests.newsfeeds;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class GetOlderActivityFeedsReq extends AbstractAuthCheckReq {
    @NotNull(message = "entity type cannot be empty")
    public EntityType eType;
    @NotBlank(message = "eId cannot be empty")
    public String eId;
    @NotBlank
    public String beforeNewsActivityId;
    @NotBlank(message = "size cannot be empty")
    public int size;
    public boolean needClustered;
    public List<String> userActions;
    public String orgId;

    public String validate() {

        if (size < 0) {
            return "invalid size";
        }
        if (StringUtils.isEmpty(eId)) {
            return "invalid id";
        }
        if (eType == EntityType.UNKNOWN) {
            return "invalid entity type";
        }
        if (StringUtils.isEmpty(beforeNewsActivityId)) {
            return "invalid beforeNewsActivityId";
        }
        return null;
    }
}
