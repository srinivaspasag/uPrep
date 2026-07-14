package com.lms.pojo.responce;

import com.lms.models.SalesCampaign;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AddSalesCampaignRes {
    public boolean       done;
    public SalesCampaign salesCampaign;
}
