package com.vedantu.content.managers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.enums.UserActionType.EventActionType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.commons.daos.TeacherAnalyticsDAO;
import com.vedantu.content.daos.DiscussionDAO;
import com.vedantu.content.daos.DoubtTransactionDAO;
import com.vedantu.content.enums.AcceptanceState;
import com.vedantu.content.enums.DoubtState;
import com.vedantu.content.event.details.DoubtsProcessingDetails;
import com.vedantu.content.models.Discussion;
import com.vedantu.content.models.DoubtTransaction;
import com.vedantu.content.models.TeacherAnalytics;
import com.vedantu.content.pojos.DoubtAssignmentDetails;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.RecordTeacherResponseReq;
import com.vedantu.content.pojos.requests.discussions.AddDiscussionReq;
import com.vedantu.content.pojos.requests.discussions.GetDiscussionReq;
import com.vedantu.content.pojos.requests.discussions.GetDiscussionsReq;
import com.vedantu.content.pojos.requests.discussions.RemoveDiscussionReq;
import com.vedantu.content.pojos.responses.RecordTeacherResponseRes;
import com.vedantu.content.pojos.responses.discussions.AddDiscussionRes;
import com.vedantu.content.pojos.responses.discussions.AssignDoubtToTeacherRes;
import com.vedantu.content.pojos.responses.discussions.GetDiscussionRes;
import com.vedantu.content.pojos.responses.discussions.RemoveDiscussionRes;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.UserTokenDAO;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.UserToken;
import com.vedantu.user.event.details.SendEmailToStudentsDetails;

public class DiscussionManager extends AbstractContentManager {

    private static final ALogger LOGGER = Logger.of(DiscussionManager.class);

    private static final long DOUBT_MAX_RESPONSE_TIME        = Play.application().configuration().getInt("doubt.max.response.time");
    private static final long DOUBT_MAX_RESPONSE_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(DOUBT_MAX_RESPONSE_TIME, TimeUnit.HOURS);
    private static final long DOUBT_MAX_SOLUTION_TIME        = Play.application().configuration().getInt("doubt.max.solution.time");
    private static final long DOUBT_MAX_SOLUTION_TIME_MILLIS = TimeUnit.MILLISECONDS.convert(DOUBT_MAX_SOLUTION_TIME, TimeUnit.HOURS);
    private static final long DOUBT_EXHAUSTION_TIME          = Play.application().configuration().getInt("doubt.exhaust.time");
    private static final long DOUBT_EXHAUSTION_TIME_MILLIS   = TimeUnit.MILLISECONDS.convert(DOUBT_EXHAUSTION_TIME, TimeUnit.HOURS);

    public static AddDiscussionRes addDiscussion(AddDiscussionReq addDissReq)
            throws VedantuException {

        validateBoardIds(addDissReq._getAllBoardIds());
        try {
            addDissReq.removeImageSrc(true);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        Discussion diss = DiscussionDAO.INSTANCE.addDiscussion(addDissReq.userId, addDissReq.name,
                addDissReq.content, addDissReq.brdIds, addDissReq.targetIds, addDissReq.tags,
                Scope.ORG, addDissReq.contentSrc);

        AddDiscussionRes addDiscussionRes = new AddDiscussionRes();
        addDiscussionRes.fromMongoModel(diss);

        generateEventAysc(addDissReq.userId, diss, EventActionType.ADD, EventType.INDEX_DISCUSSION,
                UserActionType.ADDED, true);

        DoubtTransaction doubtTransaction = new DoubtTransaction(diss._getStringId());
        DoubtTransactionDAO.INSTANCE.save(doubtTransaction);
        DoubtsProcessingDetails details = new DoubtsProcessingDetails(diss._getStringId());
        generateEventAysc(addDissReq.userId, details, EventType.PROCESS_DOUBTS);
        generateEventAysc(addDissReq.userId, details, EventType.PROCESS_DOUBTS,
                System.currentTimeMillis() + DOUBT_EXHAUSTION_TIME_MILLIS);

        return (AddDiscussionRes) annotateExtraInfo(addDissReq.userId, diss.contentSrc != null
                && diss.contentSrc.type == EntityType.ORGANIZATION ? diss.contentSrc.id : null,
                EntityType.DISCUSSION, addDiscussionRes);
    }

    public static GetDiscussionRes getDiscussionInfo(GetDiscussionReq getDissReq)
            throws VedantuException {

        Discussion diss = DiscussionDAO.INSTANCE.getById(getDissReq.id, VedantuRecordState.ACTIVE);
        if (diss == null) {
            throw new VedantuException(VedantuErrorCode.DISCUSSION_NOT_FOUND);
        }
        GetDiscussionRes getDissRes = new GetDiscussionRes();
        getDissRes.fromMongoModel(diss);
        return (GetDiscussionRes) annotateExtraInfo(getDissReq.userId, diss.contentSrc != null
                && diss.contentSrc.type == EntityType.ORGANIZATION ? diss.contentSrc.id : null,
                EntityType.DISCUSSION, getDissRes);
    }

    public static RemoveDiscussionRes removeDiscussion(RemoveDiscussionReq removeDissReq)
            throws VedantuException {

        Discussion diss = DiscussionDAO.INSTANCE.getDiscussion(removeDissReq.id);

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(removeDissReq.orgId,
                removeDissReq.userId);

        if (StringUtils.equals(removeDissReq.userId, diss.userId)
                || (orgMember != null && diss.contentSrc != null
                        && checkIfOrgIdAllowedForRemoval(diss, orgMember)
                        && orgMember.profile.isAllowedInstContentRemoval())) {
            RemoveDiscussionRes removeDissRes = new RemoveDiscussionRes();
            removeDissRes.deleted = delete(removeDissReq.userId, EventType.INDEX_DISCUSSION,
                    new SrcEntity(EntityType.DISCUSSION, removeDissReq.id));
            return removeDissRes;
        } else {

            String errorMsg = "removeDiscussion action not allowed for userId:"
                    + removeDissReq.userId + " for discussion[" + removeDissReq.id + "] ";
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED, errorMsg);
        }
    }

    private static boolean checkIfOrgIdAllowedForRemoval(Discussion diss, OrgMember orgMember) {
        // Only Learnpedia Admin or Admin of the institute to which doubt belongs; can delete it.
        return StringUtils.equals(orgMember.orgId, diss.contentSrc.id) ||
                StringUtils.equals(orgMember.orgId, Play.application().configuration()
                        .getString("learnpedia.id"));
    }

    public static ListResponse<GetDiscussionRes> getDiscussions(GetDiscussionsReq getDissReq) {

        ListResponse<GetDiscussionRes> results = getEntityInfos(getDissReq, EntityType.DISCUSSION,
                GetDiscussionRes.class, null);
        annotateExtraInfo(getDissReq.userId, getDissReq.orgId, EntityType.DISCUSSION, results.list);
        return results;
    }

    public static GetDiscussionRes fixDiscussions(GetDiscussionReq getDissReq) {
        GetDiscussionRes res = new GetDiscussionRes();
        try {
            Discussion doubt = DiscussionDAO.INSTANCE.getDiscussion(getDissReq.id);
            String orgId = OrgMemberDAO.INSTANCE.getByUserId(doubt.userId).orgId;
            if(!doubt.contentSrc.id.equals(orgId)){
                doubt.contentSrc.id = orgId;
                DiscussionDAO.INSTANCE.save(doubt);
                generateEventAysc(doubt.userId, doubt, EventActionType.UPDATE, EventType.INDEX_DISCUSSION,
                        UserActionType.UPDATED, false);
                res.voted = true;
            }else{
                res.voted = false;
            }
        } catch (VedantuException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return res;
    }

    public static ListResponse<GetDiscussionRes>
            getSimilarDiscussions(GetSimilarEntities getDissReq) {

        ListResponse<GetDiscussionRes> results = getSimilarEntityInfos(getDissReq,
                GetDiscussionRes.class, null);
        annotateExtraInfo(getDissReq.userId, getDissReq.orgId, EntityType.DISCUSSION, results.list);
        return results;
    }

    public static AssignDoubtToTeacherRes assignDoubtToTeacher(String subjectId, String discussionId) {
        long currentTimeMillis = System.currentTimeMillis();
        SendEmailToStudentsDetails details = null;
        try {
            details = new SendEmailToStudentsDetails();
        } catch (ClassNotFoundException e) {
            LOGGER.error("SendEmailToStudentsDetails class not found", e);
        }
        details.addBccRecepient("Admin Learnpedia", "admin@learnpedia.in");

        AssignDoubtToTeacherRes response = new AssignDoubtToTeacherRes();
        Queue<TeacherAnalytics> queue = TeacherAnalyticsDAO.INSTANCE
                .getTeacherAnalyticsQueue(subjectId);
        DoubtTransaction doubtTransaction = DoubtTransactionDAO.INSTANCE
                .getByDiscussionId(discussionId);
        String userId = DiscussionDAO.INSTANCE.getById(discussionId).userId;
        if (queue.isEmpty()) {
            response.success = true;
            if (!details.getBCCRecepients().isEmpty()) {
                details.setSubject("No available teachers for solving the doubt !");
                details.message = "There are no teachers available for solving this doubt :" + discussionId
                        + " , please look into this issue.";
                generateEventAysc(userId, details, EventType.SEND_EMAIL);
            }
            return response;
        }
        if (doubtTransaction.timeCreated + DOUBT_EXHAUSTION_TIME_MILLIS < currentTimeMillis) {
            response.success = true;
            if (!details.getBCCRecepients().isEmpty()) {
                details.setSubject("Doubt Exhausted");
                details.message = "This doubt has not been answered by any teacher :" + discussionId
                        + " , please look into this issue.";
                generateEventAysc(userId, details, EventType.SEND_EMAIL);
            }
            return response;
        }
        if (doubtTransaction.completed) {
            response.success = true;
            return response;
        }
        if (doubtTransaction.state.equals(DoubtState.ACCEPTED)) {
            DoubtAssignmentDetails lastAcceptedTeacherDetails = doubtTransaction.assignedTo
                    .get(doubtTransaction.assignedTo.size() - 1);
            if (lastAcceptedTeacherDetails.timeAssignedAt + DOUBT_MAX_SOLUTION_TIME_MILLIS < currentTimeMillis) {
                lastAcceptedTeacherDetails.state = AcceptanceState.ACCEPTED_NOT_SOLVED;
                TeacherAnalyticsDAO.INSTANCE
                        .cleanCurrentAssignedDoubt(lastAcceptedTeacherDetails.teacherId);
                DoubtTransactionDAO.INSTANCE.save(doubtTransaction);
            } else {
                response.success = true;
                return response;
            }
        }
        for (TeacherAnalytics teacherForAssigningDoubt; (teacherForAssigningDoubt = queue.poll()) != null;) {
            DoubtAssignmentDetails doubtAssignmentDetails = new DoubtAssignmentDetails();
            doubtAssignmentDetails.teacherId = teacherForAssigningDoubt.teacherOrgMemberId;

            OrgMember teacherOrgMember = OrgMemberDAO.INSTANCE
                    .getById(teacherForAssigningDoubt.teacherOrgMemberId);
            DoubtAssignmentDetails doubtDetails = doubtTransaction
                    .getDoubtAssignmentDetailsForTeacherId(teacherForAssigningDoubt.teacherOrgMemberId);
            if (doubtDetails != null && doubtDetails.state == AcceptanceState.REJECTED) {
                continue;
            } else if (doubtDetails != null
                    && doubtTransaction.assignedTo.contains(doubtAssignmentDetails)) {
                if (doubtDetails.timeAssignedAt + DOUBT_MAX_RESPONSE_TIME_MILLIS <= currentTimeMillis
                        && doubtDetails.state == AcceptanceState.NOT_AVAILABLE) {
                    doubtDetails.state = AcceptanceState.IGNORED;
                    DoubtTransactionDAO.INSTANCE.save(doubtTransaction);
                    continue;
                } else if (doubtDetails.state == AcceptanceState.MAYBE_LATER) {
                    if (teacherForAssigningDoubt.lastAssaignedTime + DOUBT_MAX_RESPONSE_TIME_MILLIS < currentTimeMillis) {
                        continue;
                    }
                } else if(doubtDetails.state == AcceptanceState.ACCEPTED_NOT_SOLVED){
                    continue;
                } else {
                    doAssignmentToTeacher(teacherForAssigningDoubt.teacherOrgMemberId,
                            doubtTransaction, discussionId, teacherOrgMember.userId,
                            teacherOrgMember.email);
                    break;
                }
            } else {
                doAssignmentToTeacher(teacherForAssigningDoubt.teacherOrgMemberId,
                        doubtTransaction, discussionId, teacherOrgMember.userId,
                        teacherOrgMember.email);
                break;
            }
        }
        if(doubtTransaction.state == DoubtState.REJECTED){
            if (!details.getBCCRecepients().isEmpty()) {
                details.setSubject("No teacher has accepted this doubt !");
                details.message = "No teacher has accepted this doubt :" + discussionId
                        + " , please look into this issue.";
                generateEventAysc(userId, details, EventType.SEND_EMAIL);
            }
        }
        response.success = true;
        return response;
    }

    public static boolean doAssignmentToTeacher(String teacherId,
            DoubtTransaction doubtTransaction, String discussionId, String teacherUserId,
            String teacherEmail) {
        long currentTimeMillis = System.currentTimeMillis();
        DoubtAssignmentDetails detailsAfterAssignment = new DoubtAssignmentDetails(teacherId,
                System.currentTimeMillis(), AcceptanceState.NOT_AVAILABLE);
        doubtTransaction.assignedTo.add(detailsAfterAssignment);
        doubtTransaction.state = DoubtState.ASSIGNED;
        DoubtTransactionDAO.INSTANCE.save(doubtTransaction);

        TeacherAnalytics teacherForAssigningDoubt = TeacherAnalyticsDAO.INSTANCE
                .getByTeacherId(teacherId);
        if(teacherForAssigningDoubt == null){
            return false;
        }
        teacherForAssigningDoubt.lastAssaignedTime = currentTimeMillis;
        teacherForAssigningDoubt.currentAssaignedDoubt = discussionId;
        TeacherAnalyticsDAO.INSTANCE.save(teacherForAssigningDoubt);

        UserToken userToken = UserTokenDAO.INSTANCE.getUserTokenByUserId(teacherUserId);
        if(userToken != null){
            sendNotification(discussionId, userToken.tokenId);
        }
        SendEmailToStudentsDetails emailDetails = null;
        try {
            emailDetails = new SendEmailToStudentsDetails();
        } catch (ClassNotFoundException e) {
            LOGGER.error("SendEmailToStudentsDetails class not found", e);
        }
        emailDetails.addBccRecepient("Teacher Learnpedia", teacherEmail);
        emailDetails.message = "There is a doubt for you please look into it! Here is the link of the doubt :"
                + " https://www.learnpedia.in/discussion/"+discussionId;
        generateEventAysc(teacherUserId, emailDetails, EventType.SEND_EMAIL);
        DoubtsProcessingDetails details = new DoubtsProcessingDetails();
        details.discussionId = discussionId;
        long processTime = currentTimeMillis + DOUBT_MAX_RESPONSE_TIME_MILLIS;
        generateEventAysc("", details, EventType.PROCESS_DOUBTS, processTime);
        return true;
    }

    public static RecordTeacherResponseRes recordTeacherResponse(RecordTeacherResponseReq request) {
        DoubtTransaction doubtTransaction = DoubtTransactionDAO.INSTANCE
                .getByDiscussionId(request.discussionId);

        DoubtAssignmentDetails doubtDetails = doubtTransaction
                .getDoubtAssignmentDetailsForTeacherId(request.teacherId);

        DoubtsProcessingDetails details = new DoubtsProcessingDetails();
        details.discussionId = request.discussionId;

        RecordTeacherResponseRes response = new RecordTeacherResponseRes();

        switch (request.response) {
            case ACCEPTED:
                if (!doubtTransaction.state.equals(AcceptanceState.ACCEPTED)) {
                    doubtTransaction.acceptedBy.add(request.teacherId);
                    doubtTransaction.state = DoubtState.ACCEPTED;
                    doubtDetails.state = AcceptanceState.ACCEPTED;
                    DoubtTransactionDAO.INSTANCE.save(doubtTransaction);

                    TeacherAnalytics teacher = TeacherAnalyticsDAO.INSTANCE
                            .getByTeacherId(request.teacherId);
                    teacher.lastAcceptedTime = System.currentTimeMillis();
                    TeacherAnalyticsDAO.INSTANCE.save(teacher);

                    long processTime = System.currentTimeMillis() + DOUBT_MAX_SOLUTION_TIME_MILLIS;
                    generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS,
                            processTime);
                }else {
                    response.success = false;
                    return response;
                }
                break;
            case REJECTED:
                doubtTransaction.rejectedBy.add(request.teacherId);
                doubtTransaction.state = DoubtState.REJECTED;
                doubtDetails.state = AcceptanceState.REJECTED;
                DoubtTransactionDAO.INSTANCE.save(doubtTransaction);

                generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS);
                break;
            case MAYBE_LATER:
                doubtTransaction.mayBeLater.add(request.teacherId);
                doubtTransaction.state = DoubtState.UNASSIGNED;
                doubtDetails.state = AcceptanceState.MAYBE_LATER;
                DoubtTransactionDAO.INSTANCE.save(doubtTransaction);
                long processTime = doubtDetails.timeAssignedAt + DOUBT_MAX_RESPONSE_TIME_MILLIS;
                generateEventAysc(request.teacherId, details, EventType.PROCESS_DOUBTS,processTime);
                break;
            default:
                break;
        }
        response.success = true;
        return response;
    }

    public static void markDoubtAsSolved(String discussionId, String teacherUserId) {

        DoubtTransaction transaction = DoubtTransactionDAO.INSTANCE.getByDiscussionId(discussionId);
        if (!transaction.completed) {
            transaction.completed = true;
            transaction.completedAt = System.currentTimeMillis();
            transaction.completedBy = teacherUserId;
            transaction.state = DoubtState.COMPLETED;
            DoubtTransactionDAO.INSTANCE.save(transaction);
        }
        TeacherAnalyticsDAO.INSTANCE.cleanCurrentAssignedDoubt(teacherUserId);
    }

    public static String sendNotification(String discussionId, String tokenId) {
        String response = "";
        URL url;
        try {
            String message = URLEncoder.encode("Can you answer this doubt?", "UTF-8");
            String title = URLEncoder.encode("New Doubt in Learnpedia", "UTF-8");
            String postData = "to=" + tokenId + "&data.title="+ title +"&data.message="+ message +"&data.discussionId=" + discussionId;
            url = new URL("https://fcm.googleapis.com/fcm/send");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection
                    .setRequestProperty(
                            "Authorization",
                            "key=AAAAyE9CBfI:APA91bGa87gVOmRaWu78IBEacvInMP6yM8MRwBSJ2jORW9JxZTjW1rZqe-bkSXNSiAYE9sPyLYNSFAdbd4nM5266E5NlGMrziU6T57m0G0_qkKGvqLu_GCiSItZdeU0-LfINKUALT593");
            connection.setDoOutput(true);
            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
            out.write(postData);
            out.close();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String decodedString;
            while ((decodedString = in.readLine()) != null) {
                response += decodedString;
            }
            in.close();
            return response;
        } catch (MalformedURLException e) {
            LOGGER.error("MalformedURLException in sendNotification function", e);
        } catch (IOException e) {
            LOGGER.error("IOException in sendNotification function", e);
        }
        return response;
    }
}
