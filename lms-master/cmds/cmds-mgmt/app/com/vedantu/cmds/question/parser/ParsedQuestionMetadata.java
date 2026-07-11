package com.vedantu.cmds.question.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.board.daos.BoardDAO;
import com.vedantu.board.enums.BoardContextType;
import com.vedantu.board.models.Board;
import com.vedantu.cmds.constants.QuestionSetFileConstants;
import com.vedantu.cmds.pojos.content.question.EntireQuestion;
import com.vedantu.cmds.pojos.content.question.metadata.MetadataParts;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.VedantuStringUtils;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;

public class ParsedQuestionMetadata implements Cloneable {

    public String                orgId  = null;
    private final static ALogger LOGGER = Logger.of(ParsedQuestionMetadata.class);

    public ParsedQuestionMetadata(String orgId) {

        this.orgId = orgId;
    }

    private static final String SEPERATOR   = ",";
    private static final String TOPIC_SEPERATOR   = "#";
    public String               title;

    public Set<String>          tags        = new HashSet<String>();
    public String               boardTree   = "";
    public String               source;
    public QuestionType         type;
    public Difficulty           difficulty;
    public Set<String>          topicBrdIds = new HashSet<String>(); ;
    public Set<String>          targetIds   = new HashSet<String>(); ;

    private MetadataParts       state;

    public void accumulateMetadataInfo(String runText, boolean override) throws Exception,
            VedantuException {

        if (StringUtils.isEmpty(runText)) {
            return;
        }
        String checkStart = runText.replaceAll(EntireQuestion.REGEX_HTML_STRIP, "").trim()
                .toLowerCase();
        LOGGER.debug(" Found value " + checkStart);

        if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_BOARDTREE)) {
            Logger.debug(" Prefix board tree found");
            state = MetadataParts.BOARD_TREE;
        }
        if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_TITLE)) {
            state = MetadataParts.TITLE;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_EXAM)) {
            state = MetadataParts.EXAM;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_SUBJECT)) {
            state = MetadataParts.SUBJECT;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_TOPIC)) {
            state = MetadataParts.TOPIC;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_SUBTOPIC)) {
            state = MetadataParts.SUBTOPIC;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_TAGS)) {
            state = MetadataParts.TAGS;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_SOURCE)) {
            state = MetadataParts.SOURCE;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_DIFFICULTY)) {
            state = MetadataParts.DIFFICULTY;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_TYPE)) {
            state = MetadataParts.TYPE;
        }/* this code reset state if a new question starts */
        else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_QUESTION)) {
            state = null;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_OPTIONS)) {
            state = null;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_ANSWER)) {
            state = null;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_COLUMNA)) {
            state = null;
        } else if (checkStart.startsWith("columnb:")) {
            state = null;
        } else if (checkStart.startsWith(QuestionSetFileConstants.PREFIX_SOLUTION)) {
            state = null;
        }
        if (state != null) {
            state.accumulate(this, runText, override);
        }
    }

    public void addExam(String examText) throws Exception {

        String[] examArray = StringUtils.split(examText, SEPERATOR);
        if (examArray == null) {
            return;
        }
        // if (examArray.length > 1 && batches.size() > 1) {
        // throw new Exception(
        // "multiple batches are found, not able to determine which batch :[ "
        // + examText + "] exam belong");
        // }
        // TODO read following
        // targetIds.add( get ids for exams and add here );
        Logger.info("Found exams " + Arrays.asList(examArray));
        targetIds.addAll(Arrays.asList(examArray));

    }

    public void addSubject(String subjectText, boolean override) throws Exception, VedantuException {

        String[] subjectArray = StringUtils.split(subjectText, SEPERATOR);
        if (subjectArray == null) {
            return;
        }

        if (StringUtils.isEmpty(boardTree)) {
            throw new VedantuException(VedantuErrorCode.NO_BOARD_TREE_SPECIFIED);
        }

        // TODO update with boardIds for subjectText
        List<String> subtopicsCNames = VedantuStringUtils.toCanonical(Arrays.asList(subjectArray));
        for (String subject : subtopicsCNames) {

            Logger.debug(" Looking for subject " + subject + "in board tree" + boardTree
                    + "  for ownerr " + orgId);

            Board board = BoardDAO.INSTANCE.getBoard(subject, orgId, BoardContextType.ORG,
                    boardTree);
            if (board == null) {
                Logger.debug(" No respective  subject in organziation tree : " + subject);
                throw new VedantuException(VedantuErrorCode.SUBJECT_NOT_FOUND);
            }
            this.topicBrdIds.add(board._getStringId());
        }
    }

    public void addTopic(String topicText, boolean override) throws Exception, VedantuException {

        String[] subjectArray = StringUtils.split(topicText, TOPIC_SEPERATOR);
        if (subjectArray == null) {
            return;
        }

        if (StringUtils.isEmpty(boardTree)) {
            throw new VedantuException(VedantuErrorCode.NO_BOARD_TREE_SPECIFIED);
        }
        // TODO update with boardIds for subjectText
        List<String> subtopicsCNames = VedantuStringUtils.toCanonical(Arrays.asList(subjectArray));
        for (String subject : subtopicsCNames) {
            Logger.debug(" Looking for topic " + subject);
            Board board = BoardDAO.INSTANCE.getBoard(subject, orgId, BoardContextType.ORG,
                    boardTree);
            if (board == null) {
                Logger.debug(" No respective  topic in organziation tree : " + subject);
                throw new VedantuException(VedantuErrorCode.TOPIC_NOT_FOUND);
            }
            this.topicBrdIds.add(board._getStringId());

        }
    }

    public void addSubTopic(String subTopicText) throws Exception, VedantuException {

        String[] subjectArray = StringUtils.split(subTopicText, SEPERATOR);
        if (subjectArray == null) {
            return;
        }

        if (StringUtils.isEmpty(boardTree)) {
            throw new VedantuException(VedantuErrorCode.NO_BOARD_TREE_SPECIFIED);
        }
        // TODO update with boardIds for subjectText
        List<String> subtopicsCNames = VedantuStringUtils.toCanonical(Arrays.asList(subjectArray));
        for (String subject : subtopicsCNames) {
            // TODO get board and set here
            //
            // brdIds.add();
            Logger.debug(" Looing for subtopic " + subject);

            Board board = BoardDAO.INSTANCE.getBoard(subject, orgId, BoardContextType.ORG,
                    boardTree);
            if (board == null) {
                Logger.debug(" No respective  subtopic in organziation tree : " + subject);
                throw new VedantuException(VedantuErrorCode.SUBTOPIC_NOT_FOUND);
            }
            this.topicBrdIds.add(board._getStringId());
            //

            // if no board found skip it a
        }
    }

    public void addTags(String tagsText) {

        if (this.tags == null) {
            this.tags = new HashSet<String>();
        }
        this.tags.addAll(Arrays.asList(StringUtils.split(tagsText, SEPERATOR)));
    }

    public void addSource(String sourceText) {

        this.source = sourceText;
    }

    public void addDifficulty(String levelText) {

        String[] levelArray = StringUtils.split(levelText, SEPERATOR);
        Logger.info("adding level to : " + levelText);
        if (levelArray != null) {

            Difficulty difficulty = Difficulty.valueOfKey(levelArray[0]);
            this.difficulty = difficulty;
        }

        // Logger.info("after adding level to metada [ " + new
        // Gson().toJson(this)
        // + " ]");
    }

    // private void checkForBatches(String runText) throws Exception {
    // // if (batches == null || batches.isEmpty()) {
    // // throw new Exception("no batches found");
    // // }
    // }

    @Override
    public String toString() {

        return "ParseQuestionMetadata [title:" + title + ", orgId:" + orgId + ", tags:" + tags
                + ", source:" + source + ", state:" + state + " difficulty " + difficulty
                + " type " + type + "]";
    }

    @Override
    public ParsedQuestionMetadata clone() throws CloneNotSupportedException {

        ParsedQuestionMetadata parseQuestionMetadataClone = (ParsedQuestionMetadata) super.clone();
        // TODO clone later
        parseQuestionMetadataClone.tags = new HashSet<String>(this.tags);
        parseQuestionMetadataClone.targetIds = new HashSet<String>(this.targetIds);
        parseQuestionMetadataClone.topicBrdIds = new HashSet<String>(this.topicBrdIds);
        parseQuestionMetadataClone.difficulty = this.difficulty;
        parseQuestionMetadataClone.type = this.type;
        LOGGER.debug("Cloning from globale" + this.difficulty);
        return parseQuestionMetadataClone;
    }

}
