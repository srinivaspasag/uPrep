package com.lms.pojo.responce;

import com.lms.common.vedantu.commons.pojos.requests.Location;
import com.lms.user.vedantu.user.pojo.UserBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class GetOrgResForInvoice {
    public String        name;
    public String        contactNumber;
    public String        orgThumbnail;
    public UserBasicInfo representative;
    public String        address;
    public List<Location> locations;
}
