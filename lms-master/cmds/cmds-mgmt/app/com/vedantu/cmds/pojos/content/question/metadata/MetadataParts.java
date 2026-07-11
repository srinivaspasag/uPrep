package com.vedantu.cmds.pojos.content.question.metadata;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.constants.QuestionSetFileConstants;
import com.vedantu.cmds.pojos.content.question.EntireQuestion;
import com.vedantu.cmds.question.parser.ParsedQuestionMetadata;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;

public enum MetadataParts {
    
	BOARD_TREE {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			if (questionMetadata.boardTree == null) {
				questionMetadata.boardTree = StringUtils.EMPTY;
			}
			LOGGER.debug(" Prefix board tree found" + runText);
			questionMetadata.boardTree += runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_BOARDTREE) ? StringUtils
					.substringAfter(runText, ":").trim() : runText.trim();
			// LOGGER.info("questionMetadata :  "
			// + new Gson().toJson(questionMetadata));
		}
	},
	TITLE {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			if (questionMetadata.title == null) {
				questionMetadata.title = StringUtils.EMPTY;
			}
			questionMetadata.title += runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_TITLE) ? StringUtils
					.substringAfter(runText, ":").trim() : runText.trim();

		}
	},
	EXAM {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			String exams = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_EXAM) ? StringUtils
					.substringAfter(runText, ":") : runText;
			questionMetadata.addExam(exams.trim());

		}
	},
	SUBJECT {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception,
				VedantuException {
			String subjects = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_SUBJECT) ? StringUtils
					.substringAfter(runText, ":") : runText;
			questionMetadata.addSubject(subjects.trim(), override);
		}
	},
	TOPIC {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception,
				VedantuException {
			String topics = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_TOPIC) ? StringUtils
					.substringAfter(runText.trim(), ":") : runText.trim();
			if (StringUtils.isNotEmpty(topics)
					&& StringUtils.isNotEmpty(topics.trim())) {
				questionMetadata.addTopic(topics.trim(), override);
				LOGGER.info("runtext:" + runText + "topics while parsing: "
						+ topics.trim() + " question metadata topic: "
						+ questionMetadata.toString());

			}
		}
	},
	SUBTOPIC {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception,
				VedantuException {
			String subtopics = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_SUBTOPIC) ? StringUtils
					.substringAfter(runText, ":") : runText;
			questionMetadata.addSubTopic(subtopics.trim());
		}
	},
	TAGS {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			String tags = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_TAGS) ? StringUtils
					.substringAfter(runText, ":") : runText;
			questionMetadata.addTags(tags.trim());

		}
	},
	SOURCE {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			String source = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_SOURCE) ? StringUtils
					.substringAfter(runText, ":") : runText;
			LOGGER.info("question source : " + source);
			questionMetadata.addSource(source.trim());

		}
	},
	DIFFICULTY {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception {
			String levels = runText.toLowerCase().startsWith(
					QuestionSetFileConstants.PREFIX_DIFFICULTY) ? StringUtils
					.substringAfter(runText, ":") : runText;
			// questionMetadata.addLevel(levels);

			LOGGER.debug("Found difficult in file " + levels);
			questionMetadata.difficulty = Difficulty.valueOfKey(levels.trim()
					.toLowerCase());
		}
	},
	TYPE {
		@Override
		public void accumulate(ParsedQuestionMetadata questionMetadata,
				String runText, boolean override) throws Exception,
				VedantuException {
			String type = runText.toLowerCase()
					.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "")
					.startsWith("type:") ? StringUtils.substringAfter(runText,
					":") : runText;
			LOGGER.debug(" Question type from parsing:" + type.trim());
			QuestionType temporaryType = QuestionType.valueOfKey(type.trim());
			if (temporaryType == QuestionType.UNKNOWN) {
				throw new VedantuException(
						VedantuErrorCode.INVALID_QUESTION_TYPE);
			}
			questionMetadata.type = temporaryType;
		}
	};

	public static ALogger	LOGGER	= Logger.of(MetadataParts.class);

	public abstract void accumulate(ParsedQuestionMetadata questionMetadata,
			String runText, boolean override) throws Exception,
			VedantuException;
}
