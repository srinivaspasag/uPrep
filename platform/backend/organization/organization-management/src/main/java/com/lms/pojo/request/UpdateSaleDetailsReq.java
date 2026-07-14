package com.lms.pojo.request;

import com.lms.billing.model.PaymentItem;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import javax.validation.constraints.NotBlank;


@Getter
@Setter
public class UpdateSaleDetailsReq extends AbstractAuthCheckReq {
	@NotBlank(message="orgId should not be null")
    public String orgId;
	@NotBlank(message="saleDetailsId should not be null")
    public String saleDetailsId;
	@NotBlank(message="targetUserId should not be null")
    public String targetUserId;
	@NotBlank(message="targetOrgMemberId should not be null")
    public String targetOrgMemberId;

    public List<PaymentItem> paymentItems;

}
