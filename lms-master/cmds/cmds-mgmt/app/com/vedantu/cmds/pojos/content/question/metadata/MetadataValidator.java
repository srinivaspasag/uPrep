package com.vedantu.cmds.pojos.content.question.metadata;

import java.util.HashSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.question.parser.ParsedQuestionMetadata;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.tests.Metadata;

public class MetadataValidator {
	private static ALogger	LOGGER			= Logger.of(MetadataValidator.class);
	private String			errorMessage	= null;

	public boolean validate(ParsedQuestionMetadata questionParseMetadata,
			Metadata metadata, boolean required) {
		boolean isValid = true;// TODO remove this //false;
		if (questionParseMetadata == null) {
			errorMessage = "parsed metadata from document is null";
			LOGGER.error(errorMessage);
			return false;
		}
		if ((questionParseMetadata.type == null || questionParseMetadata.type == QuestionType.UNKNOWN)
				&& required) {
			errorMessage = "question type is required";
			LOGGER.error(errorMessage);
			return false;
		}

		metadata.type = questionParseMetadata.type;

		if (StringUtils.isNotEmpty(questionParseMetadata.source)) {
			metadata.origRefName = questionParseMetadata.source.trim();
		}
		if (questionParseMetadata.tags != null) {
			metadata.tags = questionParseMetadata.tags;
		}
		if (CollectionUtils.isEmpty(questionParseMetadata.topicBrdIds)) {
			return false;
		}
		metadata.brdIds = new HashSet<String>();
		metadata.brdIds.addAll(questionParseMetadata.topicBrdIds);
		if (CollectionUtils.isNotEmpty(questionParseMetadata.targetIds)) {
			if (metadata.targetIds == null) {
				metadata.targetIds = new HashSet<String>();
			}
			metadata.targetIds.addAll(questionParseMetadata.targetIds);
		}
		LOGGER.debug("Logging difficulty where we are creating metadata from parsedMetadata : " + questionParseMetadata.difficulty);
		
		if (questionParseMetadata.difficulty == null) {
			metadata.difficulty = Difficulty.UNKNOWN;
		} else {
			metadata.difficulty = questionParseMetadata.difficulty;
		}

		metadata.name = StringUtils.isEmpty(questionParseMetadata.title) ? StringUtils.EMPTY
				: questionParseMetadata.title.trim();

		// TODO check for all brdIds, targetIds,
		Logger.info("validate metadata returns :" + isValid);
		return isValid;
	}

	// private boolean validateCenters(List<String> parseCenters,
	// Set<Center> dbCenters, Metadata metadata) {
	// boolean valid = false;
	// if (parseCenters != null && dbCenters != null) {
	// if (parseCenters.isEmpty()) {
	// errorMessage = "parseCenters list can not be empty";
	// showNewMessage = false;
	// LOGGER.error(errorMessage);
	// return valid;
	// }
	//
	// for (String center : parseCenters) {
	// Logger.info("validating center : " + center);
	// Center nCenter = new Center(center.trim());
	// valid = dbCenters.contains(nCenter);
	// if (!valid) {
	// errorMessage = "Center [" + center
	// + "] not present in database";
	// showNewMessage = false;
	// LOGGER.error(errorMessage);
	// return valid;
	// } else {
	// metadata.centers.add(nCenter);
	// }
	// }
	// } else {
	// errorMessage = "found centers[" + parseCenters
	// + "], available centers[" + dbCenters + "]";
	// showNewMessage = false;
	// LOGGER.error(errorMessage);
	// }
	// Logger.info("validate centers returns :" + valid);
	// return valid;

	// }
	/*
	 * private boolean validateCourses(Organization organization, List<Subject>
	 * parseSubjects, Metadata metadata, String batchName, String stream,
	 * List<String> centers, boolean required) { boolean valid = false; if
	 * (parseSubjects != null) { if (parseSubjects.isEmpty() && required) {
	 * errorMessage = "parseSubject list is empty"; showNewMessage = false;
	 * Logger.info("validate subject returns :" + valid +
	 * ", as the parseSubject list is empty"); return valid; } //
	 * Logger.info("is valid: "+false); List<Stream> streams = new
	 * ArrayList<Stream>(organization.streams); Logger.info("streams: " +
	 * StringUtils.join(streams, " , ")); Logger.info("stream : " + stream);
	 * Stream strm = streams.get(streams.indexOf(new Stream(stream)));
	 * 
	 * if (strm == null) { showNewMessage = false; errorMessage = "Stream[" +
	 * stream + "] for [batch:" + batchName + ", stream:" + stream +
	 * ", centers:" + centers + "] not present in database"; return valid; }
	 * 
	 * Set<String> departmentIds = strm.departmentIds;
	 * 
	 * for (Subject sub : parseSubjects) { Logger.info("validating subject : " +
	 * sub); DBObject query = new BasicDBObject( ConstantsGlobal.DEPARTMENT_ID,
	 * new BasicDBObject("$in", departmentIds.toArray()));
	 * query.put(ConstantsGlobal.NAME, sub.name); DBObject result =
	 * MongoDBCollection .findOne(query, Course.class); Course course = null; if
	 * (result == null) { showNewMessage = false; errorMessage = "Subject[" +
	 * sub.name + "] for [batch:" + batchName + ", stream:" + stream +
	 * ", centers:" + centers + "] not present in database"; return valid; }
	 * course = ObjectMapperUtil.convertToMongoModel(result, Course.class);
	 * Set<com
	 * .vedantu.content.metadata.question.metadata.pojos.organizations.Topic>
	 * topics = new
	 * HashSet<com.vedantu.content.metadata.question.metadata.pojos.
	 * organizations.Topic>( MapUtils.getMapUniqueValue(course.topics));
	 * CourseData courseData = new CourseData(course.name, course.code,
	 * course._getStringId(), new
	 * HashSet<com.vedantu.content.metadata.question.metadata
	 * .pojos.organizations.Topic>()); valid = validateTopics(sub.topics,
	 * topics, courseData, required); if (valid) { if (metadata.courses == null)
	 * { metadata.courses = new HashSet<CourseData>(); }
	 * metadata.courses.add(courseData); } } } else if (!required) {
	 * LOGGER.error("parseSubjects :" + parseSubjects); valid = true; } else {
	 * errorMessage = "found subjects[" + parseSubjects +
	 * "] can not be empty or null "; showNewMessage = false;
	 * LOGGER.error(errorMessage); } Logger.info("validate subjects returns :" +
	 * valid); return valid; }
	 * 
	 * private boolean validateTopics(List<Topic> parseTopics,
	 * Set<com.vedantu.content
	 * .metadata.question.metadata.pojos.organizations.Topic> courseTopics,
	 * CourseData courseData, boolean required) { boolean valid = false; if
	 * (parseTopics != null && courseTopics != null) { if (parseTopics.isEmpty()
	 * && required) { errorMessage = "parse topic list is empty"; showNewMessage
	 * = false; Logger.info("validate topic returns :" + valid +
	 * ", as the parse topic list is empty"); return valid; } for (Topic t :
	 * parseTopics) { Logger.info("validating topic : " + t.name); valid =
	 * courseTopics .contains(new
	 * com.vedantu.content.metadata.question.metadata.pojos.organizations.Topic(
	 * t.name.trim())); if (!valid) { showNewMessage = false; errorMessage =
	 * "Topic [" + t.name + "] for subject[" + courseData.name +
	 * "] not present in database"; return valid; } for
	 * (com.vedantu.content.metadata.question.metadata.pojos.organizations.Topic
	 * dbTopic : courseTopics) {
	 * com.vedantu.content.metadata.question.metadata.pojos.organizations.Topic
	 * nTopic = new
	 * com.vedantu.content.metadata.question.metadata.pojos.organizations.Topic(
	 * t.name.trim()); if (dbTopic.equals(nTopic)) { valid =
	 * validateSubTopics(t.subTopics, dbTopic.subTopics, t.name); if (valid) {
	 * if (courseData.topics == null) { courseData.topics = new
	 * HashSet<com.vedantu
	 * .content.metadata.question.metadata.pojos.organizations.Topic>(); } for
	 * (SubTopic subT : t.subTopics) { if (subT != null) {
	 * com.vedantu.content.metadata
	 * .question.metadata.pojos.organizations.SubTopic nSubTopic = new
	 * com.vedantu
	 * .content.metadata.question.metadata.pojos.organizations.SubTopic(
	 * subT.name); if (CollectionUtils .isNotEmpty(nSubTopic.subSubTopics)) {
	 * nSubTopic.subSubTopics = new HashSet<String>( subT.subSubTopics); }
	 * nTopic.addSubTopic(nSubTopic); } } courseData.topics.add(nTopic); } }
	 * 
	 * }
	 * 
	 * } } else if (!required) { LOGGER.error("parseTopics :" + parseTopics);
	 * valid = true; } else { errorMessage = "found topics[" + parseTopics +
	 * "], available topics[" + courseTopics + "]"; showNewMessage = false;
	 * LOGGER.error(errorMessage); } Logger.info("validate topics returns :" +
	 * valid); return valid; }
	 * 
	 * private boolean validateSubTopics(List<SubTopic> parseSubTopics,
	 * Set<com.vedantu
	 * .content.metadata.question.metadata.pojos.organizations.SubTopic>
	 * courseSubTopics, String topic) { boolean valid = false; if
	 * (parseSubTopics != null && courseSubTopics != null) { if
	 * (parseSubTopics.isEmpty()) { Logger.info("validate subtopic returns :" +
	 * true); return true; } for (SubTopic subT : parseSubTopics) {
	 * Logger.info("validating subtopic : " + subT); valid = courseSubTopics
	 * .contains(new
	 * com.vedantu.content.metadata.question.metadata.pojos.organizations
	 * .SubTopic( subT.name.trim())); if (!valid) { showNewMessage = false;
	 * errorMessage = "SubTopic [" + subT.name + "] for topic [" + topic +
	 * "] not present in database"; return valid; }
	 * 
	 * } } else { errorMessage = "found subtopics[" + parseSubTopics +
	 * "], not present in available subtopics[" + courseSubTopics + "]";
	 * showNewMessage = false; LOGGER.error(errorMessage); }
	 * Logger.info("validate subtopic returns :" + valid); return valid; }
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
}
