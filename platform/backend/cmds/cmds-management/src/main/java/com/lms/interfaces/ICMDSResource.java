package com.lms.interfaces;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.events.searchdetails.CMDSResourceDetails;

public interface ICMDSResource {

    CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model);

}
