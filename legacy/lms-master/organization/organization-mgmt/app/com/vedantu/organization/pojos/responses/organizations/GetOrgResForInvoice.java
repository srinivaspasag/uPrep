package com.vedantu.organization.pojos.responses.organizations;

import java.util.List;

import com.vedantu.commons.pojos.Location;
import com.vedantu.user.pojos.UserBasicInfo;

public class GetOrgResForInvoice {
    public String        name;
    public String        contactNumber;
    public String        orgThumbnail;
    public UserBasicInfo representative;
    public String        address;
    public List<Location>                locations;
}
