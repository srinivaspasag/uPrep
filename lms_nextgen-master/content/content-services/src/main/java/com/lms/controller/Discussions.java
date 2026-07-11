package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;
import com.lms.services.DiscussionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RequestMapping("/discussions")
@RestController
public class Discussions {
    @Autowired
    private DiscussionsService discussionsServiceImpl;
    @PostMapping("/addDiscussion")
    public ResponseEntity<VedantuResponse> addDiscussion(@Valid AddDiscussionReq addDiscussionReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.adddiscussion(addDiscussionReq));
    }

    @PostMapping("/removeDiscussion")
    public ResponseEntity<VedantuResponse> removeDiscussion(@Valid RemoveDiscussionReq removeDiscussionReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.removediscussion(removeDiscussionReq));
    }

    @PostMapping("/getDiscussionInfo")
    public ResponseEntity<VedantuResponse> getDiscussionInfo(@Valid GetDiscussionReq getDiscussionReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.getdiscussionInfo(getDiscussionReq));
    }

    @PostMapping("/getDiscussions")
    public ResponseEntity<VedantuResponse> getDiscussions(@Valid GetDiscussionsReq getDiscussionsReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.getdiscussions(getDiscussionsReq));
    }

    @PostMapping("/fixDiscussions")
    public ResponseEntity<VedantuResponse> fixDiscussions(@Valid GetDiscussionReq getDiscussionReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.fixdiscussions(getDiscussionReq));
    }

    @PostMapping("/recordTeacherResponse")
    public ResponseEntity<VedantuResponse> recordTeacherResponse(@Valid RecordTeacherResponseReq recordTeacherResponseReq) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.recordteacherResponse(recordTeacherResponseReq));
    }

    @PostMapping("/getSimilarDiscussions")
    public ResponseEntity<VedantuResponse> getSimilarDiscussions(@Valid GetSimilarEntities getSimilarEntities) throws VedantuException {
        return ResponseEntity.ok(discussionsServiceImpl.getSimilardiscussions(getSimilarEntities));
    }
}

