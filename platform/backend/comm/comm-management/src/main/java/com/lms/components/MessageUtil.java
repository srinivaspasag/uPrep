package com.lms.components;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojo.OrgMemberBasicInfo;
import com.lms.pojos.news.details.UserNewsEntityDetails;
import com.lms.user.vedantu.user.pojo.UserInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MessageUtil {
	private static final Logger logger = LoggerFactory.getLogger(MessageUtil.class);
	@Autowired
	private StatusFeedsComponent statusFeedsComponent;

	public SrcEntity populateUserNewsEntityDetails(String orgId, SrcEntity srcEntity) {

		List<SrcEntity> srcEntities = new ArrayList<SrcEntity>();
		srcEntities.add(srcEntity);
		List<SrcEntity> updatedSrcEntities = populateUserNewsEntityDetails(orgId, srcEntities);

		if (CollectionUtils.isNotEmpty(updatedSrcEntities)) {
			srcEntity = updatedSrcEntities.iterator().next();

			logger.info("Decorated entity " + srcEntity);
		}
		return srcEntity;
	}

	public List<SrcEntity> populateUserNewsEntityDetails(String orgId, List<SrcEntity> srcEntities) {

		logger.info(" Collection user information for all users in news set user count " + srcEntities.size());

		if (srcEntities == null || srcEntities.isEmpty()) {
			logger.error("srcEntities set is null or empty");
			return null;
		}
		List<String> userIds = new ArrayList<String>();
		List<SrcEntity> userList = new ArrayList<SrcEntity>();
		for (SrcEntity entity : srcEntities) {
			userIds.add(entity.id);
		}
		Map<String, ModelBasicInfo> orgMemberInfoSet = statusFeedsComponent.getUserInfoMap(orgId, userIds);

		logger.info("Received cursor from mongo for " + orgMemberInfoSet.keySet());
		if (CollectionUtils.isEmpty(orgMemberInfoSet.entrySet())) {
			logger.error("received cursor is null");
			return null;
		}
		UserInfo userInfo = null;
		for (SrcEntity entity : srcEntities) {

			userInfo = (UserInfo) orgMemberInfoSet.get(entity.id);

			if (userInfo == null) {
				logger.debug("No user info found for" + entity.id);
				continue;
			}

			UserNewsEntityDetails userDetails = new UserNewsEntityDetails();
			userDetails.id = (userInfo instanceof OrgMemberBasicInfo) ? ((OrgMemberBasicInfo) userInfo).userId
					: userInfo.id;

			userDetails.type = EntityType.USER;
			if (!StringUtils.isEmpty(userInfo.firstName)) {
				userDetails.firstName = userInfo.firstName;
				logger.info(" first name found for user " + userDetails.firstName + " for user id:" + userDetails.id);

			} else {

				logger.debug(" No first name found for user " + userDetails.id);

			}
			if (!StringUtils.isEmpty(userInfo.lastName)) {
				userDetails.lastName = userInfo.lastName;

			} else {
				userDetails.lastName = "";
			}

			userDetails.thumbnail = userInfo.thumbnail;
			if (userInfo instanceof OrgMemberBasicInfo) {
				userDetails.profile = ((OrgMemberBasicInfo) userInfo).profile;
			}
			userList.add(userDetails);
		}

		return userList;
	}

}
