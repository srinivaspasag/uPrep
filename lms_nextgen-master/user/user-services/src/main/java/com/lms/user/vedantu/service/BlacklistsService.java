package com.lms.user.vedantu.service;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.user.vedantu.user.requests.BlacklistEmailReq;
import com.lms.user.vedantu.user.requests.EmailSubscribeReq;
import com.lms.user.vedantu.user.requests.GetBlacklistEmailReq;
import com.lms.user.vedantu.user.requests.GetBlacklistedEmailsReq;

public interface BlacklistsService {
    VedantuResponse getBlackList(BlacklistEmailReq blacklistEmailReq);

    VedantuResponse removeFromBlacklist(BlacklistEmailReq blacklistEmailReq);

    VedantuResponse getBlacklistInfo(GetBlacklistEmailReq getBlacklistEmailReq);

    VedantuResponse getBlacklistedEmails(GetBlacklistedEmailsReq getBlacklistedEmailsReq);
}
