package com.vedantu.cmds.mgmt.interfaces;

import com.vedantu.cmds.models.event.search.details.CMDSResourceDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;

public interface ICMDSResource {

    public CMDSResourceDetails getCMDSResourceDetails(VedantuBaseMongoModel model);

}
