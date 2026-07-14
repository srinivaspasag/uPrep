package com.lms.components;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.enums.OrgMemberProfile;
import com.lms.event.details.PostRemarkDetails;
import com.lms.managers.AbstractContentManager;
import com.lms.models.OrgMember;
import com.lms.models.Remark;
import com.lms.pojo.OrgMemberBasicInfo;
import com.lms.pojos.RemarkInfo;
import com.lms.repository.OrgMemberRepo;
import com.lms.repository.RemarkRepo;
import com.lms.requests.remarks.AddRemarksReq;
import com.lms.requests.remarks.GetRemarksReq;
import com.lms.response.remarks.AddRemarksRes;
import com.lms.response.remarks.GetRemarksRes;
import com.lms.user.vedantu.user.pojo.UserInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class RemarksComponent extends AbstractContentManager {
    private static final Logger logger = LoggerFactory.getLogger(RemarksComponent.class);
    @Autowired
    private OrgMemberRepo orgMemberRepo;
    @Autowired
    private RemarkRepo remarkRepo;
    @Autowired
    private MongoTemplate mongoTemplate;

    public AddRemarksRes addRemark(AddRemarksReq request) {
        // TODO check if request.userId is teacher for orgId else return
        OrgMember orgMember = orgMemberRepo
                .findByOrgIdAndUserId(request.orgId, request.userId);
        if (orgMember == null || orgMember.profile != OrgMemberProfile.TEACHER
                || request.targetUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.POSTING_NOT_ALLOWED);
        }

        AddRemarksRes response = new AddRemarksRes();
        try {
            logger.debug("Remarks being for" + request.targetUserId + " a by " + request.userId);
            Remark remark = addRemark(request.targetUserId, request.userId,
                    request.content, request.orgId);

            response.info = (RemarkInfo) getBasicInfo(remark);

            response.info.addedFor = getUserInfo(request.orgId, remark.provideeId);
            response.info.addedFor.id = (response.info.addedFor instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) response.info.addedFor).userId
                    : response.info.addedFor.id;
            response.info.addedBy = getUserInfo(request.orgId, remark.providerId);
            response.info.addedBy.id = (response.info.addedBy instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) response.info.addedBy).userId
                    : response.info.addedBy.id;

            PostRemarkDetails details = new PostRemarkDetails();
            details.provideeId = request.targetUserId;
            details.providerId = request.userId;
            details.remarkId = response.info.id;
            generateEventAysc(request.userId, details, EventType.POST_REMARK);
        } catch (Exception exception) {
            throw new VedantuException(VedantuErrorCode.SERVICE_ERROR);

        }
        response.success = true;
        return response;

    }

    public Remark addRemark(String provideeId, String providerId, String content, String orgId) {

        Remark remark = new Remark(providerId, provideeId, content, orgId);
        remarkRepo.save(remark);
        return remark;
    }

    public ModelBasicInfo getBasicInfo(Remark info) {

        RemarkInfo remark = new RemarkInfo(info._getStringId(), null, EntityType.REMARK,
                info.timeCreated, info.lastUpdated, info.providerId, 0, info.recordState);
        remark.content = info.content;

        return remark;
    }

    public GetRemarksRes getRemarksForUser(GetRemarksReq request) {
        // validate if UserId is parent of student
        // // Current quickfix
        logger.debug("Getting remarks for" + request.targetUserId + " as requested by "
                + request.userId);

        OrgMember orgMember = orgMemberRepo
                .findByOrgIdAndUserId(request.orgId, request.userId);
        logger.debug(" OrgMember " + orgMember);
        if (orgMember == null
                || (orgMember.profile != OrgMemberProfile.TEACHER && !request.userId
                .equals(request.targetUserId))) {
            throw new VedantuException(VedantuErrorCode.VIEWING_NOT_ALLOWED);
        }

        logger.debug("Getting remarks for" + request.targetUserId + " as requested by "
                + request.userId);

        GetRemarksRes getRemarkRes = new GetRemarksRes();
        AtomicLong totalHits = new AtomicLong(0);
        List<Remark> remarks = getRemarksFor(request.targetUserId,
                request.providerId, request.start, request.size, request.sortAscending, totalHits);
        Set<String> userIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(remarks)) {

            for (Remark remark : remarks) {
                userIds.add(remark.provideeId);
                userIds.add(remark.providerId);
            }
            Map<String, ModelBasicInfo> userInfos = getUserInfoMap(request.orgId, userIds);

            for (Remark remark : remarks) {
                RemarkInfo info = (RemarkInfo) getBasicInfo(remark);

                info.addedFor = (UserInfo) userInfos.get(remark.provideeId);
                info.addedFor.id = (info.addedFor instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) info.addedFor).userId
                        : info.addedFor.id;

                info.addedBy = (UserInfo) userInfos.get(remark.providerId);
                info.addedBy.id = (info.addedBy instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) info.addedBy).userId
                        : info.addedBy.id;
                getRemarkRes.list.add(info);

            }
        }
        getRemarkRes.totalHits = totalHits.longValue();
        return getRemarkRes;

    }

    public List<Remark> getRemarksFor(String provideeId, String providerId, int start, int size,
                                      boolean ascending, AtomicLong totalHits) {

        logger.debug(" Querying in db now ");
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (!StringUtils.isEmpty(provideeId)) {
            // remarkQuery = remarkQuery.filter("provideeId", provideeId);
            criteria.and("provideeId").is(provideeId);
        }
        if (!StringUtils.isEmpty(providerId)) {
            //remarkQuery = remarkQuery.filter("providerId", providerId);
            criteria.and("providerId").is(providerId);
        }
        query.addCriteria(criteria);
        query.skip(start).limit(size);
        //remarkQuery = remarkQuery.offset(start).limit(size);
        if (ascending) {
            //remarkQuery.order("timeCreated");
            query.with(Sort.by(Sort.Direction.ASC, "timeCreated"));

        } else {
            //remarkQuery.order("-timeCreated");
            query.with(Sort.by(Sort.Direction.DESC, "timeCreated"));
        }
        logger.debug(query.toString());
        List<Remark> remarks = mongoTemplate.find(query, Remark.class);
        totalHits.set(remarks.size());
        return remarks;
    }

}
