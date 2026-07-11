package com.vedantu.eventbus.processors.challenges;

import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class ChallengeNewsActivityGenerator implements IProcessor {
// NewsActivityGeneratorProcessor newsActivityGeneratorProcessor = NewsActivityGeneratorProcessor.INSTANCE;

    @Override
    public Status process(IConsumable consumable) {

//        Event event = (Event) consumable;
//        ChallengeSearchIndexDetails details = (ChallengeSearchIndexDetails) event
//                .fetchEventDetails();
//        DBObject query = new BasicDBObject(ConstantsGlobal.CHALLENGE_ID,
//                details.challengeId);
//        Logger.log4j.info("challenge taken news activity query :" + query);
//        boolean process = true;
//        int start = 0;
//        while (process) {
//            DBCursor cursor = MongoDBCollection.getMongoDBResult(query,
//                    ChallengeTaken.class, start, ChallengeUtilCommon.DEFAULT_BATCH_SIZE);
//            if (cursor != null && cursor.size() > 0) {
//                start = cursor.size();
//                Logger.log4j.info("cursor size :" + start + ", counr:" + cursor.count());
//                List<ChallengeTaken> challengeTakens = new ArrayList<ChallengeTaken>();
//                while (cursor.hasNext()) {
//                    ChallengeTaken challengeTaken = ObjectMapperUtil.convertToMongoModel(
//                            cursor.next(), ChallengeTaken.class);
//                    challengeTakens.add(challengeTaken);
//                }
//                for (ChallengeTaken challengeTaken : challengeTakens) {
//                    Logger.log4j.info("generating newsactivity for challenegTaken : "
//                            + challengeTaken);
//                    NewsActivity newsActivity = getNewsActivity(challengeTaken, event,
//                            details);
//                    boolean result = newsActivity == null ? false : NewsActivityGenerator
//                            .getInstance().generate(newsActivity, null);
//                    Logger.log4j.info("result : " + result);
//                    if (!result) {
//                        return Status.FAILURE;
//                    }
//                }
//            } else {
//                Logger.log4j.info("null cursor ");
//                process = false;
//            }
//        }
        return Status.SUCCESS;
    }
	//
	// private NewsActivity getNewsActivity(ChallengeTaken challengeTaken, Event
	// event,
	// ChallengeSearchIndexDetails details) {
	// NewsActivity newsActivity = new NewsActivity();
	// newsActivity.eType = event.getType();
	// newsActivity.time = event.getTime();
	// newsActivity.actor = new NewsEntity(challengeTaken.userId,
	// EntityType.USER);
	// newsActivity.srcOwner = new NewsEntity(details.userId, EntityType.USER);
	// newsActivity.src = new NewsEntity(details.challengeId,
	// EntityType.CHALLENGE);
	// newsActivity.sendNewsFeed = false;
	// MultiplierPowerType mPowerType = challengeTaken.multiplierPower == null ?
	// MultiplierPowerType.SINGLE
	// : challengeTaken.multiplierPower;
	// ChallengeTakenInfo info = new
	// ChallengeTakenInfo(challengeTaken.totalPoint,
	// challengeTaken.basePoint, mPowerType.getMultiplier(),
	// challengeTaken.timeTaken, challengeTaken.success, challengeTaken.hint,
	// details.title, details.challengeId, details.duration, details.userId,
	// details.entities.get(0).id);
	// info.actionType = UserActionType.ATTEMPTED;
	// newsActivity.info = info;
	// return newsActivity;
	// }
}
