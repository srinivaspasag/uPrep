package com.lms.user.vedantu.service.impl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.user.vedantu.service.BlacklistsService;
import com.lms.user.vedantu.user.enums.EmailBlacklistAction;
import com.lms.user.vedantu.user.model.EmailBlacklist;
import com.lms.user.vedantu.user.pojo.responce.BlacklistEmailRes;
import com.lms.user.vedantu.user.pojo.responce.GetBlacklistEmailRes;
import com.lms.user.vedantu.user.pojo.responce.GetBlacklistedEmailsRes;
import com.lms.user.vedantu.user.repository.EmailBlacklistRepo;
import com.lms.user.vedantu.user.requests.BlacklistEmailReq;
import com.lms.user.vedantu.user.requests.GetBlacklistEmailReq;
import com.lms.user.vedantu.user.requests.GetBlacklistedEmailsReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BlacklistsServiceImpl implements BlacklistsService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private EmailBlacklistRepo emailBlacklistRepo;

    @Override
    public VedantuResponse getBlackList(BlacklistEmailReq blacklistEmailReq) {

        if (blacklistEmailReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        BlacklistEmailRes res = addEmailToBlacklist(blacklistEmailReq);

        return new VedantuResponse(res);

    }

    @Override
    public VedantuResponse removeFromBlacklist(BlacklistEmailReq blacklistEmailReq) {
        if (blacklistEmailReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
                    
        }
      
        BlacklistEmailRes res = removeEmailFromBlacklist(blacklistEmailReq);

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getBlacklistInfo(GetBlacklistEmailReq getBlacklistEmailReq) {

        if (getBlacklistEmailReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS) ;
        }

        GetBlacklistEmailRes res =  BlacklistInfo(getBlacklistEmailReq);

        return new VedantuResponse(res);
    }

    @Override
    public VedantuResponse getBlacklistedEmails(GetBlacklistedEmailsReq getBlacklistedEmailsReq) {

        if (getBlacklistedEmailsReq==null) {
            throw new VedantuException(VedantuErrorCode.MISSING_PARAMETERS);
        }

        GetBlacklistedEmailsRes res = blacklistedEmails(getBlacklistedEmailsReq);

        return new VedantuResponse(res);
    }

    private GetBlacklistedEmailsRes blacklistedEmails(GetBlacklistedEmailsReq getBlacklistedEmailsReq) {

        GetBlacklistedEmailsRes res = new GetBlacklistedEmailsRes();
        Optional<EmailBlacklist> emailInfo=null;
        Pageable pageWithElements = PageRequest.of(getBlacklistedEmailsReq.start, getBlacklistedEmailsReq.size);
        if(getBlacklistedEmailsReq.getQuery()!=null)
            emailInfo=emailBlacklistRepo.findByEmail(getBlacklistedEmailsReq.getQuery().toLowerCase().trim());
        Page<EmailBlacklist> allBlackList = emailBlacklistRepo.findAllSortBy(pageWithElements, getBlacklistedEmailsReq.getSortOrder());
        if(emailInfo!=null){
            res.setTotalHits(1);
            res.getList().add(toBlacklistEmailPojo(emailInfo.get()));
        }
        else{
            res.setTotalHits(allBlackList.stream().count());
            for (EmailBlacklist info : allBlackList) {
                res.getList().add(toBlacklistEmailPojo(info));
            }

        }
        return res;
    }

    private GetBlacklistEmailRes BlacklistInfo(GetBlacklistEmailReq getBlacklistEmailReq) throws VedantuException {

        Optional<EmailBlacklist> getEmailBlacklist = emailBlacklistRepo.findByEmail(getBlacklistEmailReq.getEmail().toLowerCase().trim());

        if (!getEmailBlacklist.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE, "email : "
                    + getBlacklistEmailReq.getEmail() + " was never blacklisted");
        }
        EmailBlacklist emailBlacklist= getEmailBlacklist.get();
        GetBlacklistEmailRes res = toBlacklistEmailPojo(emailBlacklist);
        return res;
    }

    private GetBlacklistEmailRes toBlacklistEmailPojo(EmailBlacklist emailBlacklist) {
        GetBlacklistEmailRes res = new GetBlacklistEmailRes();
        res.setEmail( emailBlacklist.getEmail());
        res.setBlacklisted(emailBlacklist.isBlacklisted());
        res.setInfos(emailBlacklist.getInfos());
        return res;
    }

    private BlacklistEmailRes removeEmailFromBlacklist(BlacklistEmailReq blacklistEmailReq) {
        BlacklistEmailRes res = new BlacklistEmailRes();
        Optional<EmailBlacklist> getEmailBlacklist = emailBlacklistRepo.findByEmail(blacklistEmailReq.getEmail());
        EmailBlacklist emailBlacklist = null;

        if (!getEmailBlacklist.isPresent()) {
            emailBlacklist = new EmailBlacklist(blacklistEmailReq.getEmail());
        }
        if (getEmailBlacklist.isPresent()) {
            emailBlacklist = getEmailBlacklist.get();
        }
        emailBlacklist.addBlacklistInfo(blacklistEmailReq.getReason(), EmailBlacklistAction.UNBLOCKED);
        emailBlacklistRepo.save(emailBlacklist);
        res.setSuccess(emailBlacklist != null);
        return res;

    }

    private BlacklistEmailRes addEmailToBlacklist(BlacklistEmailReq blacklistEmailReq) {
        BlacklistEmailRes res = new BlacklistEmailRes();

        Optional<EmailBlacklist> getEmailBlacklist = emailBlacklistRepo.findByEmail(blacklistEmailReq.getEmail());
        EmailBlacklist emailBlacklist=null;
        if (!getEmailBlacklist.isPresent()) {
             emailBlacklist = new EmailBlacklist(blacklistEmailReq.getEmail().toLowerCase());
        }
        if(getEmailBlacklist.isPresent())
         emailBlacklist = getEmailBlacklist.get();
        logger.debug("adding  blacklist info for email: " + blacklistEmailReq.getEmail());
        emailBlacklist.addBlacklistInfo(blacklistEmailReq.getReason(), EmailBlacklistAction.BLOCKED);
        emailBlacklistRepo.save(emailBlacklist);
        res.setSuccess(emailBlacklist != null);
        return res;
    }
}
