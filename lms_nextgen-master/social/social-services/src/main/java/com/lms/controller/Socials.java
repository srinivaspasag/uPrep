package com.lms.controller;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.requests.AddEntityUserActionReq;
import com.lms.requests.GetEntityUserActionUsersReq;
import com.lms.requests.RemoveEntityUserActionReq;
import com.lms.requests.SendEmailReq;
import com.lms.services.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/socials")
public class Socials {
    @Autowired
    private SocialService socialService;

    @PostMapping("/view")
    public ResponseEntity<VedantuResponse> view(AddEntityUserActionReq addEntityUserActionReq) {
        return ResponseEntity.ok(socialService.view(addEntityUserActionReq));
    }

    @PostMapping("/upVote")
    public ResponseEntity<VedantuResponse> upVote(AddEntityUserActionReq addEntityUserActionReq) {
        return ResponseEntity.ok(socialService.upVote(addEntityUserActionReq));
    }

    @PostMapping("/unFollow")
    public ResponseEntity<VedantuResponse> unFollow(RemoveEntityUserActionReq removeEntityUserActionReq) {
        return ResponseEntity.ok(socialService.unfollow(removeEntityUserActionReq));
    }

    @PostMapping("/follow")
    public ResponseEntity<VedantuResponse> follow(AddEntityUserActionReq addEntityUserActionReq) {
        return ResponseEntity.ok(socialService.follow(addEntityUserActionReq));
    }

    @PostMapping("/getFollowers")
    public ResponseEntity<VedantuResponse> getFollowers(GetEntityUserActionUsersReq getEntityUserActionUsersReq) {
        return ResponseEntity.ok(socialService.getfollowers(getEntityUserActionUsersReq));
    }

    @PostMapping("/getViewers")
    public ResponseEntity<VedantuResponse> getViewers(GetEntityUserActionUsersReq getEntityUserActionUsersReq) {
        return ResponseEntity.ok(socialService.getViewers(getEntityUserActionUsersReq, UserActionType.VIEWED));
    }

    @PostMapping("/completed")
    public ResponseEntity<VedantuResponse> completed(AddEntityUserActionReq addEntityUserActionReq) {
        return ResponseEntity.ok(socialService.completed(addEntityUserActionReq, UserActionType.COMPLETED, true));
    }


    @PostMapping("/getVoters")
    public ResponseEntity<VedantuResponse> getVoters(GetEntityUserActionUsersReq getEntityUserActionUsersReq) {
        return ResponseEntity.ok(socialService.getvoters(getEntityUserActionUsersReq));
    }

    @PostMapping("/sendEmail")
    public ResponseEntity<VedantuResponse> sendEmail(SendEmailReq sendEmailReq) {
        return ResponseEntity.ok(socialService.sendemail(sendEmailReq));
    }
}
