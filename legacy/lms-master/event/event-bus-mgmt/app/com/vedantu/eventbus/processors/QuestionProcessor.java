package com.vedantu.eventbus.processors;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.content.search.details.QuestionSearchIndexDetails;
import com.vedantu.eventbus.processors.chained.ChainedProcessor;
import com.vedantu.eventbus.processors.chained.ChainedProcessors;
import com.vedantu.events.models.Event;
import com.vedantu.events.task.apis.IConsumable;
import com.vedantu.events.task.apis.IProcessor;
import com.vedantu.events.task.enums.Status;

public class QuestionProcessor extends ChainedProcessors implements IProcessor {

	private static final ALogger LOGGER = Logger.of(QuestionProcessor.class);

	public QuestionProcessor() {
		super(new ChainedProcessor(new QuestionSearchIndexProcessor(), false)
		/*
		 * , new ChainedProcessor(PointIncrementerProcessor.INSTANCE, false),
		 * new ChainedProcessor(NewsActivityGeneratorProcessor.INSTANCE, true )
		 */
		);
	}

	@Override
	public Status process(IConsumable consumable) {
		Status result = super.process(consumable);

		try {
			Event event = (Event) consumable;
			QuestionSearchIndexDetails questionDetails = (QuestionSearchIndexDetails) event
					.fetchEventDetails();
			updateDiagrams(questionDetails);
		} catch (Exception exception) {
			LOGGER.info("Failed while updating diagrams :", exception);
			// TODO diagram updating is not critical but we will have to handle
			// it differently
			// may be through background jobs
		}

		return result;
	}

	//
	// /** Update diagrams from question / options / grid html content */
	public void updateDiagrams(QuestionSearchIndexDetails questionDetails) {
		//
		// Set<String> newBrdIds = new HashSet<String>();
		// Set<String> newTargetIds = new HashSet<String>();
		// for( BoardSearchEntity brdSearchEntity : questionDetails.boards ){
		// newBrdIds.add(brdSearchEntity.brdId);
		// }
		//
		// for( BoardSearchEntity targetSearchEntity : questionDetails.targets
		// ){
		// newTargetIds.add(targetSearchEntity.brdId);
		// }
		//
		// if( questionDetails.userAction == UserActionType.ADDED )
		// {
		// Map<String,Set<String>> imageTypeMap = new HashMap<String,
		// Set<String>>();
		// QuestionImageUtil.removeImageSrcUrl(questionDetails.content,
		// imageTypeMap);
		//
		// if (CollectionUtils.isNotEmpty(questionDetails.options)) {
		// for (String htmlContentForOption : questionDetails.options) {
		// QuestionImageUtil.removeImageSrcUrl(htmlContentForOption,
		// imageTypeMap);
		// }
		// }
		// if (MapUtils.isNotEmpty(questionDetails.grid)) {
		// Set<String> keys = questionDetails.grid.keySet();
		// if (CollectionUtils.isNotEmpty(keys)) {
		// for (String key : keys) {
		// List<String> htmlContentForGridOptions =
		// questionDetails.grid.get(key);
		// if (CollectionUtils.isNotEmpty(htmlContentForGridOptions)) {
		// for (String htmlContentForOption : htmlContentForGridOptions) {
		// QuestionImageUtil.removeImageSrcUrl(htmlContentForOption,
		// imageTypeMap);
		// }
		// }
		// }
		// }
		// }
		//
		// Set<String> diagramIdSet = imageTypeMap.get("d");
		// if( diagramIdSet != null ){
		// for( String diagramId : diagramIdSet ){
		// Logger.log4j.info("Updating diagram: " + diagramId +
		// " with tags, brdIds & newTargetIds in question Id " +
		// questionDetails.qid );
		// DiagramUtil.updateDiagram(EntityType.QUESTION,questionDetails.qid ,
		// diagramId,questionDetails.tags , newBrdIds , newTargetIds );
		// }
		// }
		// }
		//
	}
}
