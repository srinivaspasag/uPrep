package com.vedantu.comm.managers.news.populators;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.news.details.ChallengeNewsDetails;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.models.challenges.Challenge;
import com.vedantu.mongo.IVedantuModel;

public class ChallengeNewsPopulator extends AbstractEntityDetailsPopulator {

	public final static ChallengeNewsPopulator INSTANCE = new ChallengeNewsPopulator();
	private final static ALogger LOGGER = Logger
			.of(ChallengeNewsPopulator.class);

	@Override
	public void populate(String orgId, String userId,
			Set<SrcEntity> newsEntities,
			Map<SrcEntity, SrcEntity> srcEntityDetails, EntityType entityType) {

		super.populate(orgId, userId, newsEntities, srcEntityDetails,
				entityType);
	}

	@Override
	public SrcEntity populate(String orgId, String userId,
			SrcEntity newsEntity, Map<String, IVedantuModel> modelDetailMap) {

		LOGGER.debug(" Populating challenge " + newsEntity.id);
		ChallengeNewsDetails details = null;
		Challenge challenge = (Challenge) modelDetailMap.get(newsEntity.id);
		if (challenge == null) {
			return details;
		}
		details = new ChallengeNewsDetails(newsEntity.type, newsEntity.id);
		details.name = challenge.name;
		details.timeCreated = challenge.timeCreated;
		details.contentSrc = challenge.contentSrc;
		if( CollectionUtils.isNotEmpty(challenge.entities)){
		    details.qId= challenge.entities.get(0).id;
		}
		switch (NewsFeedSecurityVaildator.get().contextType) {
		case EMAIL:
//
//			ChallengeTaken token = ChallengeTakenDAO.INSTANCE
//					.getChallengeTaken(challenge._getStringId(),
//							NewsFeedSecurityVaildator.get().getUserId());
//			ChallengeTakenBasicInfo info = (ChallengeTakenBasicInfo) token
//					.toBasicInfo();
//			MultiplierPowerType mPowerType = info.multiplierPower == null ? MultiplierPowerType.SINGLE
//					: info.multiplierPower;
//			details.succeded = info.success;
			break;

		default:
			break;
		}
		
		return details;
	}

}
