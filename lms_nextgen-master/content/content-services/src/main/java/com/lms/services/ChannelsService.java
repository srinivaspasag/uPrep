package com.lms.services;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddChannelReq;
import com.lms.pojos.requests.AddContentToChannelReq;
import com.lms.pojos.requests.EditChannelReq;
import com.lms.pojos.requests.GetChannelsReq;

public interface ChannelsService {
    VedantuResponse addChannel(AddChannelReq addChannelReq);

    VedantuResponse getChannels(GetChannelsReq getChannelsReq);

    VedantuResponse editChannel(EditChannelReq editChannelReq);

    VedantuResponse addContentToChannel(AddContentToChannelReq addContentToChannelReq, boolean addOnIndexing);


}
