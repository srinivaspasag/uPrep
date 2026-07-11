package com.lms.pojo.request;

import com.lms.models.Campaign;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetCampaignsRes {
    public Campaign campaign;

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "GetCampaignsRes [campaign=" + campaign + "]";
    }
}
