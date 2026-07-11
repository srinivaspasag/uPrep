package com.vedantu.comm.managers;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.mutable.MutableLong;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.daos.RemarkDAO;
import com.vedantu.comm.event.details.PostRemarkDetails;
import com.vedantu.comm.models.mongo.Remark;
import com.vedantu.comm.pojos.RemarkInfo;
import com.vedantu.comm.requests.remarks.AddRemarksReq;
import com.vedantu.comm.requests.remarks.GetRemarksReq;
import com.vedantu.comm.response.remarks.AddRemarksRes;
import com.vedantu.comm.response.remarks.GetRemarksRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;
import com.vedantu.user.pojos.UserInfo;

public class RemarksManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(RemarksManager.class);

    private RemarksManager() {

    }

    public static GetRemarksRes getRemarksForUser(GetRemarksReq request) throws VedantuException {

        // validate if UserId is parent of student
        // // Current quickfix
        LOGGER.debug("Getting remarks for" + request.targetUserId + " as requested by "
                + request.userId);

        OrgMember orgMember = OrgMemberDAO.INSTANCE
                .getMemberByUserId(request.orgId, request.userId);
        LOGGER.debug(" OrgMember " + orgMember);
        if (orgMember == null
                || (orgMember.profile != OrgMemberProfile.TEACHER && !request.userId
                        .equals(request.targetUserId))) {
            throw new VedantuException(VedantuErrorCode.VIEWING_NOT_ALLOWED);
        }

        LOGGER.debug("Getting remarks for" + request.targetUserId + " as requested by "
                + request.userId);

        GetRemarksRes getRemarkRes = new GetRemarksRes();
        MutableLong totalHits = new MutableLong(0);
        List<Remark> remarks = RemarkDAO.INSTANCE.getRemarksFor(request.targetUserId,
                request.providerId, request.start, request.size, request.sortAscending, totalHits);
        Set<String> userIds = new HashSet<String>();
        if (CollectionUtils.isNotEmpty(remarks)) {

            for (Remark remark : remarks) {
                userIds.add(remark.provideeId);
                userIds.add(remark.providerId);
            }
            Map<String, ModelBasicInfo> userInfos = getUserInfoMap(request.orgId, userIds);

            for (Remark remark : remarks) {
                RemarkInfo info = (RemarkInfo) RemarkDAO.INSTANCE.getBasicInfo(remark);

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

    public static AddRemarksRes addRemark(AddRemarksReq request) throws VedantuException {

        // TODO check if request.userId is teacher for orgId else return
        OrgMember orgMember = OrgMemberDAO.INSTANCE
                .getMemberByUserId(request.orgId, request.userId);
        if (orgMember == null || orgMember.profile != OrgMemberProfile.TEACHER
                || request.targetUserId.equals(request.userId)) {
            throw new VedantuException(VedantuErrorCode.POSTING_NOT_ALLOWED);
        }

        AddRemarksRes response = new AddRemarksRes();
        try {
            LOGGER.debug("Remarks being for" + request.targetUserId + " a by " + request.userId);
            Remark remark = RemarkDAO.INSTANCE.addRemark(request.targetUserId, request.userId,
                    request.content, request.orgId);

            response.info = (RemarkInfo) RemarkDAO.INSTANCE.getBasicInfo(remark);

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

}
