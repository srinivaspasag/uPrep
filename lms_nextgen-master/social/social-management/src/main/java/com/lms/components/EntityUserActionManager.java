package com.lms.components;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.managers.AbstractContentManager;
import com.lms.models.EntityUserActionMapping;
import com.lms.requests.AddEntityUserActionReq;
import com.lms.requests.GetEntityUserActionUsersReq;
import com.lms.requests.RemoveEntityUserActionReq;
import com.lms.response.EntityUserActionRes;
import com.lms.response.EntityUserActionUsersRes;
import com.lms.user.vedantu.user.pojo.UserInfo;
import com.lms.utils.EntityUserActionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class EntityUserActionManager extends AbstractContentManager {
	@Autowired
	private EntityUserActionUtils entityUserActionUtils;

	@Autowired
	private MongoTemplate mongoTemplate;

	public EntityUserActionRes addEntityUserAction(AddEntityUserActionReq addReq, UserActionType actionType,
												   boolean allowDuplicates) {
		entityUserActionUtils.isSocialActionAllowed(addReq.entity.type, addReq.entity.id);
		EntityUserActionRes addRes = new EntityUserActionRes();

		addRes.processed = entityUserActionUtils.addEntityUserAction(addReq.userId, addReq.entity, addReq.context,
				actionType, allowDuplicates);
		return addRes;
	}

	public EntityUserActionRes removeEntityUserAction(RemoveEntityUserActionReq removeReq,
													  UserActionType actionType) throws VedantuException {

		entityUserActionUtils.isSocialActionAllowed(removeReq.entity.type, removeReq.entity.id);
		EntityUserActionRes removeRes = new EntityUserActionRes();
		removeRes.processed = entityUserActionUtils.removeEntityUserAction(removeReq.userId,
				removeReq.entity, actionType);
		return removeRes;
	}

	public EntityUserActionRes addEntityUserAction(AddEntityUserActionReq upVoteReq, UserActionType actionType) {
		return addEntityUserAction(upVoteReq, actionType, false);
	}

	/*public  EntityUserActionUsersRes getUserFollowings(GetUserFollowingsReq getReq)
			throws VedantuException {

		AtomicLong totalHits = new AtomicLong();
		List<String> userIds = getUserEntityActionEntityIds(
				getReq.userId, EntityType.USER, UserActionType.FOLLOWING, getReq.start,
				getReq.size, getReq.orderBy, getReq.sortOrder, totalHits);
		Map<String, ModelBasicInfo> usersMap = getUserInfoMap(getReq.orgId, userIds);
		EntityUserActionUsersRes res = new GetFollowingsRes();
		res.totalHits = totalHits.getValue();
		for (String userId : userIds) {
			res.list.add((UserInfo) usersMap.get(userId));
		}
		return res;
	}*/
	public EntityUserActionUsersRes getEntityUserActionUsers(
			GetEntityUserActionUsersReq getReq, UserActionType actionType) throws VedantuException {

		AtomicLong totalHits = new AtomicLong();
		List<String> userIds = getEntityUserActionByIds(getReq.entity,
				actionType, getReq.start, getReq.size, getReq.orderBy, getReq.sortOrder, totalHits);
		Map<String, ModelBasicInfo> usersMap = getUserInfoMap(null, userIds);
		EntityUserActionUsersRes res = new EntityUserActionUsersRes();
		res.totalHits = totalHits.get();
		for (String userId : userIds) {
			res.list.add((UserInfo) usersMap.get(userId));
		}
		return res;
	}

	public List<String> getEntityUserActionByIds(SrcEntity target, UserActionType actionType,
												 int start, int size, String orderBy, String sortOrder, AtomicLong totalHits) {

		Query query = new Query();
		Criteria criteria = new Criteria();
		criteria.and(ConstantsGlobal.TARGET_DOT_ID).is(target.getId());

		criteria.and(ConstantsGlobal.TARGET_DOT_TYPE).is(target.getType().name());
		criteria.and(ConstantsGlobal.ACTION_TYPE).is(actionType.name());
		if (StringUtils.isEmpty(orderBy)) {
			orderBy = ConstantsGlobal.TIME_CREATED;
		}

		List<EntityUserActionMapping> results = mongoTemplate.find(query.addCriteria(criteria), EntityUserActionMapping.class);

		if (totalHits != null) {
			totalHits.set(results.stream().count());
		}
		List<String> userIds = new ArrayList<String>();
		for (EntityUserActionMapping e : results) {
			userIds.add(e.userId);
		}
		return userIds;
	}
}
