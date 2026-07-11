package com.lms.pojos.responce;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAttemptedEntityRes extends SrcEntity {
    private static final long serialVersionUID = 1L;

    public long endTime;

    public GetAttemptedEntityRes(EntityType type, String id, long endTime) {

        super(type, id);
        this.endTime = endTime;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{endTime:").append(endTime).append(", type:").append(type).append(", id:")
                .append(id).append("}");
        return builder.toString();
    }
}
