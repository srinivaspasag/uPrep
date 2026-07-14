package com.vedantu.social.managers;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.pojos.requests.SendEmailReq;
import com.vedantu.content.pojos.requests.socials.AddEntityUserActionReq;
import com.vedantu.content.pojos.requests.socials.GetEntityUserActionUsersReq;
import com.vedantu.content.pojos.requests.socials.GetUserFollowingsReq;
import com.vedantu.content.pojos.requests.socials.RemoveEntityUserActionReq;
import com.vedantu.content.pojos.responses.SendEmailRes;
import com.vedantu.content.pojos.responses.socials.EntityUserActionRes;
import com.vedantu.content.pojos.responses.socials.EntityUserActionUsersRes;
import com.vedantu.content.pojos.responses.socials.GetFollowingsRes;
import com.vedantu.content.utils.EntityUserActionUtils;
import com.vedantu.user.event.details.SendEmailToStudentsDetails;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.pojos.UserInfo;

public class EntityUserActionManager extends AbstractContentManager {

    public static EntityUserActionRes addEntityUserAction(AddEntityUserActionReq addReq,
            UserActionType actionType) throws VedantuException {

        return addEntityUserAction(addReq, actionType, false);
    }

    public static EntityUserActionRes addEntityUserAction(AddEntityUserActionReq addReq,
            UserActionType actionType, boolean allowDuplicates) throws VedantuException {

        isSocialActionAllowed(addReq.entity.type, addReq.entity.id);
        EntityUserActionRes addRes = new EntityUserActionRes();

        addRes.processed = EntityUserActionUtils.addEntityUserAction(addReq.userId, addReq.entity,
                addReq.context, actionType, allowDuplicates);
        return addRes;
    }

    public static EntityUserActionRes removeEntityUserAction(RemoveEntityUserActionReq removeReq,
            UserActionType actionType) throws VedantuException {

        isSocialActionAllowed(removeReq.entity.type, removeReq.entity.id);
        EntityUserActionRes removeRes = new EntityUserActionRes();
        removeRes.processed = EntityUserActionUtils.removeEntityUserAction(removeReq.userId,
                removeReq.entity, actionType);
        return removeRes;
    }

    public static EntityUserActionUsersRes getEntityUserAction_Users(
            GetEntityUserActionUsersReq getReq, UserActionType actionType) throws VedantuException {

        MutableInt totalHits = new MutableInt();
        List<String> userIds = EntityUserActionDAO.INSTANCE.getEntityUserActionByIds(getReq.entity,
                actionType, getReq.start, getReq.size, getReq.orderBy, getReq.sortOrder, totalHits);
        Map<String, ModelBasicInfo> usersMap = getUserInfoMap(null, userIds);
        EntityUserActionUsersRes res = new EntityUserActionUsersRes();
        res.totalHits = totalHits.getValue();
        for (String userId : userIds) {
            res.list.add((UserInfo) usersMap.get(userId));
        }
        return res;
    }

    public static EntityUserActionUsersRes getUserFollowings(GetUserFollowingsReq getReq)
            throws VedantuException {

        MutableInt totalHits = new MutableInt();
        List<String> userIds = EntityUserActionDAO.INSTANCE.getUserEntityActionEntityIds(
                getReq.userId, EntityType.USER, UserActionType.FOLLOWING, getReq.start,
                getReq.size, getReq.orderBy, getReq.sortOrder, totalHits);
        Map<String, ModelBasicInfo> usersMap = getUserInfoMap(getReq.orgId, userIds);
        EntityUserActionUsersRes res = new GetFollowingsRes();
        res.totalHits = totalHits.getValue();
        for (String userId : userIds) {
            res.list.add((UserInfo) usersMap.get(userId));
        }
        return res;
    }

    public static SendEmailRes sendEmail(SendEmailReq request) {
        SendEmailRes response = new SendEmailRes();
        SendEmailToStudentsDetails details = null;
        String newLine = System.getProperty("line.separator");
        try {
            details = new SendEmailToStudentsDetails();
        } catch (ClassNotFoundException e) {
            LOGGER.error("SendEmailToStudentsDetails class not found", e);
        }
        if (request.fromForm.equals("PROGRAMS")) {
            details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
            details.setSubject("Student has a query!");
            details.message = "Mobile number of prospective client : " + request.number;
        } else if (request.fromForm.equals("ENQUIRY")) {
            details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
            details.setSubject("Franchise Enquiry!");
            details.message = "Name :" + request.name + newLine + "Email :" + request.email
                    + newLine + "Number :" + request.number + newLine + "City :" + request.city + newLine + "Investment :" + request.investment + newLine
                    + "Questions if any :" + request.que_message;
        }
        else if (request.fromForm.equals("INSTITUTES")) {
            details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
            details.setSubject("B2B Enquiry!");
            details.message = "Name :" + request.name + newLine + "Email :" + request.email
                    + newLine + "Message :" + request.message + newLine + "Number :"
                    + request.number;
        }
        else if (request.fromForm.equals("PRICING")) {
            details.addBccRecepient("Deepak", "deepak.bunde@learnpedia.in");
            details.setSubject("Enterprise Pack Plan Enquiry!");
            details.message = "Name :" + request.name + newLine + "Email :" + request.email
                    + newLine + "Message :" + request.message + newLine + "Number :"
                    + request.number;
        }
        else {
            details.addBccRecepient("Admin Learnpedia", "admin@learnpedia.in");
            details.setSubject("Get in touch!");
            details.message = "Name :" + request.name + newLine + "Email :" + request.email
                    + newLine + "Message :" + request.message + newLine + "Number :"
                    + request.number;
        }
        generateEventAysc("", details, EventType.SEND_EMAIL);
        response.success = true;
        return response;
    }

}
