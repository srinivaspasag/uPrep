package com.lms.controller;


import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddChannelReq;
import com.lms.pojos.requests.AddContentToChannelReq;
import com.lms.pojos.requests.EditChannelReq;
import com.lms.pojos.requests.GetChannelsReq;
import com.lms.services.ChannelsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Channels")
public class Channels
{
    @Autowired
    ChannelsService channelsServiceImpl;

    @PostMapping("/addChannel")
    public ResponseEntity<VedantuResponse> addChannel(AddChannelReq addChannelReq) {
        return ResponseEntity.ok(channelsServiceImpl.addChannel(addChannelReq));
    }

    @PostMapping("/getChannels")
    public ResponseEntity<VedantuResponse> getChannels(GetChannelsReq getChannelsReq) {
        return ResponseEntity.ok(channelsServiceImpl.getChannels(getChannelsReq));
    }

    @PostMapping("/editChannel")
    public ResponseEntity<VedantuResponse> editChannel(EditChannelReq editChannelReq) {
        return ResponseEntity.ok(channelsServiceImpl.editChannel(editChannelReq));
    }

    // Implementation not completed, Elastic search pending.
    @PostMapping("/addContentToChannel")
    public ResponseEntity<VedantuResponse> addContentToChannel(AddContentToChannelReq addContentToChannelReq) {
        return ResponseEntity.ok(channelsServiceImpl.addContentToChannel(addContentToChannelReq, false));
    }


}
