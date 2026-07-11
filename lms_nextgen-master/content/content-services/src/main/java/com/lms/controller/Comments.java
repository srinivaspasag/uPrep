package com.lms.controller;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddCommentReq;
import com.lms.pojos.requests.GetCommentReq;
import com.lms.pojos.requests.GetCommentsReq;
import com.lms.pojos.requests.GetScheduleReq;
import com.lms.services.CommentService;
import com.lms.services.serviceImpl.CommentServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/comments")
public class Comments {
    @Autowired
    private CommentService commentServiceImpl;
    @PostMapping("/addComment")
    public ResponseEntity<VedantuResponse> addComment(@Valid AddCommentReq addCommentReq) throws VedantuException {
        return ResponseEntity.ok(commentServiceImpl.addcomment(addCommentReq));
    }
    @PostMapping("/getComment")
    public ResponseEntity<VedantuResponse> getComment(@Valid GetCommentReq getCommentReq) throws VedantuException {
        return ResponseEntity.ok(commentServiceImpl.getcomment(getCommentReq));
    }
    @PostMapping("/getComments")
    public ResponseEntity<VedantuResponse> getComments(@Valid GetCommentsReq getCommentsReq) throws VedantuException {
        return ResponseEntity.ok(commentServiceImpl.getcomments(getCommentsReq));
    }
}
