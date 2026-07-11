package com.lms.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class GetConversationUsersReq extends AbstractAuthCheckReq {

    @NotBlank(message = "conversationId should not be null")
    public String conversationId;
    public int start;
    public int size;
    public String orgId;
    public List<String> excludeUserIds;
}
