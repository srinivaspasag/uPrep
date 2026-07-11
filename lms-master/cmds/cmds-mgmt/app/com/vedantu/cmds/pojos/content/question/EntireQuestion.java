package com.vedantu.cmds.pojos.content.question;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.enums.QuestionParts;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.pojos.content.question.metadata.MetadataValidator;
import com.vedantu.cmds.pojos.content.solution.metadata.GridSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.MCQsolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.NumericSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SCQSolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.SolutionInfo;
import com.vedantu.cmds.pojos.content.solution.metadata.TextSolutionInfo;
import com.vedantu.cmds.question.parser.ParsedQuestionMetadata;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.tests.Metadata;

public class EntireQuestion {

    public static final String      REGEX_EXCEPT_ALPHANUMERIC_COMMA = "[^a-zA-Z0-9,]";
    public static final String      REGEX_ANSWER_STRING_MATCH       = "([a-z]|,|[1-2])+";
    private static final ALogger    LOGGER                          = Logger.of(EntireQuestion.class);
    private static final String     regex                           = "^([\\s]*)([(]?)([a-zA-Z0-9]{1,3})([)\\.])"; // /
                                                                                                                   // TODO
                                                                                                                   // check
                                                                                                                   // what
                                                                                                                   // it
                                                                                                                   // does?
                                                                                                                   // seems
                                                                                                                   // like
                                                                                                                   // it
                                                                                                                   // will
                                                                                                                   // have
                                                                                                                   // only
                                                                                                                   // 3
                                                                                                                   // values
    public static final String      REGEX_HTML_STRIP                = "<[^>]*>";
    private static final Pattern    p                               = Pattern.compile(regex);
    public QuestionType             type;
    public String                   answer                          = StringUtils.EMPTY;
    public List<String>             cola                            = new ArrayList<String>();
    public List<String>             colb                            = new ArrayList<String>();
    public Map<String, Set<String>> gridAnswer                      = new HashMap<String, Set<String>>();
    public QuestionParts            state;

    public OptionFormat             formattedOptions                = new OptionFormat();
    public QuestionFormat           formattedQuestion               = new QuestionFormat();
    public List<SolutionFormat>     formattedSolutions              = new ArrayList<SolutionFormat>();
    // public List<String> qulutions = new ArrayList<String>();
    public ParsedQuestionMetadata    metadata                        = null;
    public List<HintFormat>         formattedHints                  = new ArrayList<HintFormat>();

    public EntireQuestion(String orgId) {

        metadata = new ParsedQuestionMetadata(orgId);
    }

    public void accumulateQuestionInfo(boolean isStart, String runText, String htmlText,
            List<String> uuids, boolean inHTML, boolean imgInBase64, boolean newPara) {

        if (StringUtils.isEmpty(htmlText.trim())) {
            LOGGER.error("empty html text: " + htmlText);
            return;
        }
        boolean changeOfState = false;
        if (isStart) {
            LOGGER.info("started question :" + isStart);
            LOGGER.info("runText start before: " + runText);
            String checkStart = runText.trim().toLowerCase().replaceAll(REGEX_HTML_STRIP, "")
                    .trim();
            LOGGER.info("check start after: " + checkStart);
            if (checkStart.startsWith("question:")) {
                state = QuestionParts.TEXT;
                changeOfState = true;
            } else if (checkStart.startsWith("type:")) {
                state = QuestionParts.TYPE;
                changeOfState = true;
            } else if (checkStart.startsWith("options:")) {
                state = QuestionParts.OPTIONS;
                changeOfState = true;
            } else if (checkStart.startsWith("answer:")) {
                state = QuestionParts.ANSWER;
                changeOfState = true;
            } else if (checkStart.startsWith("solution:")) {
                state = QuestionParts.SOLUTION;
                changeOfState = true;
            } else if (checkStart.startsWith("hints:")) {
                state = QuestionParts.HINT;
                changeOfState = true;
            } else if (checkStart.startsWith("columna:")) {
                state = QuestionParts.COLUMNA;
                changeOfState = true;
            } else if (checkStart.startsWith("columnb:")) {
                state = QuestionParts.COLUMNB;
                changeOfState = true;
            } // this will set the state to null
            else if (checkStart.startsWith("title:")) {
                state = null;
            } else if (checkStart.startsWith("institute:")) {
                state = null;
            } else if (checkStart.startsWith("batch:")) {
                state = null;
            } else if (checkStart.startsWith("center:")) {
                state = null;
            } else if (checkStart.startsWith("stream:")) {
                state = null;
            } else if (checkStart.startsWith("section:")) {
                state = null;
            } else if (checkStart.startsWith("exam:")) {
                state = null;
            } else if (checkStart.startsWith("subject:")) {
                state = null;
            } else if (checkStart.startsWith("topic:")) {
                state = null;
            } else if (checkStart.startsWith("subtopic:")) {
                state = null;
            } else if (checkStart.startsWith("subsubtopic:")) {
                state = null;
            } else if (checkStart.startsWith("tags:")) {
                state = null;
            } else if (checkStart.startsWith("source:")) {
                state = null;
            } else if (checkStart.startsWith("level:")) {
                state = null;
            }
        }

        if (null != state) {
            if (!changeOfState && newPara
                    && (state == QuestionParts.TEXT || state == QuestionParts.SOLUTION)) {
                htmlText = StringUtils.isNotEmpty(htmlText.trim()) ? "<br>" + htmlText : htmlText;
            }
            state.accumulate(this, isStart, htmlText, uuids, inHTML, imgInBase64);
        }
    }

    @Override
    public String toString() {

        StringBuilder s = new StringBuilder("Q:{");
        s.append(", ").append("type:").append(type);
        s.append(", ").append("text:").append(formattedQuestion.newText);
        s.append(", ").append("options:").append(formattedOptions.newOptions);
        s.append(", ").append("answer:").append(answer);
        s.append("}");
        return s.toString();
    }

    public static List<String> formatOptions(List<String> options, List<String> optionOrder) {

        List<String> convertedOption = new ArrayList<String>();
        int n = 0;
        if (CollectionUtils.isNotEmpty(options)) {
            for (String option : options) {
                Matcher m = p.matcher(option);
                if (m.find()) {
                    n++;
                    String opn = m.group();
                    optionOrder.add(opn);
                    LOGGER.info("old option is : " + option + " to be replaced text : " + opn);
                    // option = option.replaceFirst(regex, "(" + n + ") ");
                    option = option.replaceFirst(regex, "");
                    LOGGER.info("new option is : " + option);
                    convertedOption.add(option);
                } else if (n != 0) {
                    int lastIndex = convertedOption.size() - 1;
                    String lastOption = convertedOption.get(lastIndex) + option;
                    convertedOption.set(lastIndex, lastOption);
                }
            }
        }
        return convertedOption;
    }

    public CMDSQuestion toQuestion(EntireQuestion entireQuestion, String orgId, String userId,
            String questionSetName, Metadata metadata) throws VedantuException {

        SolutionInfo solutionInfo = null;
        if (entireQuestion.type == null) {
            entireQuestion.type = entireQuestion.metadata != null
                    && entireQuestion.metadata.type != QuestionType.UNKNOWN ? entireQuestion.metadata.type
                    : QuestionType.UNKNOWN;
            if (entireQuestion.type == null) {
                throw new VedantuException(VedantuErrorCode.UNKNOWN_QUESTION_TYPE);
            }
        }
        if (entireQuestion.type == QuestionType.UNKNOWN || entireQuestion.type == null) {
            LOGGER.error("unknow question type: " + entireQuestion.type);
            throw new VedantuException(VedantuErrorCode.UNKNOWN_QUESTION_TYPE);
        }
        // change the options to 1,2,3...etc
        List<String> optionOrder = new ArrayList<String>();
        LOGGER.info("question option format : " + entireQuestion.formattedOptions.newOptions);
        int n = 0;
        List<String> convertedOption = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(entireQuestion.formattedOptions.newOptions)) {
            for (String option : entireQuestion.formattedOptions.newOptions) {
                Matcher m = p.matcher(option);
                if (m.find()) {
                    n++;
                    String opn = m.group();
                    optionOrder.add(opn);
                    LOGGER.info("old option is : " + option + " to be replaced text : " + opn);
                    // option = option.replaceFirst(regex, "(" + n + ") ");
                    option = option.replaceFirst(regex, "");
                    LOGGER.info("new option is : " + option);
                    convertedOption.add(option);
                } else if (n != 0) {
                    int lastIndex = convertedOption.size() - 1;
                    String lastOption = convertedOption.get(lastIndex) + option;
                    convertedOption.set(lastIndex, lastOption);
                }
            }
            // if (!correctOptionFormat) {
            // throw new Exception(
            // "invalid answer, options format (bullets formating not allowed) : original: "
            // + entireQuestion.of.originalOptions
            // + " , converted: "
            // + entireQuestion.of.newOptions);
            // }
        }
        // Checking for duplicate options and throw error if they are duplicate
        Set<String> convertedOptionSet = new HashSet<String>();
        convertedOptionSet.addAll(convertedOption);
        if (convertedOptionSet.size() < convertedOption.size()) {
            throw new VedantuException(VedantuErrorCode.OPTION_DUPLICATION_OCCURED,
                    "Invalid options.Please review the options carefully. "
                            + StringUtils.join(convertedOption, " , "));

        }
        // Fix ends
        entireQuestion.formattedOptions.optionOrder = optionOrder;
        entireQuestion.formattedOptions.originalOptions = entireQuestion.formattedOptions.newOptions;
        entireQuestion.formattedOptions.newOptions = convertedOption;

        if (entireQuestion.type == QuestionType.SCQ) {
            LOGGER.debug(" Parsing scq");
            if (StringUtils.isEmpty(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT,
                        "answer not provided in question");
            }

            entireQuestion.answer = entireQuestion.answer.replaceAll(
                    REGEX_EXCEPT_ALPHANUMERIC_COMMA, "");
            String ans = entireQuestion.answer.split(",")[0];

            if (StringUtils.isEmpty(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT,
                        "answer not provided in question");
            }

            LOGGER.debug("Matching for  answer " + ans);
            int ansi = 0;
            for (String ansOptn : entireQuestion.formattedOptions.optionOrder) {
                ansi++;
                String providedOption = stripAnsFormat(ansOptn);
                String providedAnswer = stripAnsFormat(ans);
                LOGGER.debug("Tryng to match   provided answer " + providedOption
                        + " for an provided option" + providedAnswer);
                if (StringUtils.equalsIgnoreCase(providedOption, providedAnswer)) {
                    LOGGER.debug("Matched  answer " + ans + " for an option" + ansOptn);
                    break;
                }
            }
            solutionInfo = new SCQSolutionInfo(entireQuestion.formattedOptions, ansi + "");
        } else if (entireQuestion.type == QuestionType.MCQ || entireQuestion.type == QuestionType.MATRIX || entireQuestion.type == QuestionType.PARA) {
            LOGGER.debug(" Parsing mcq");
            if (StringUtils.isEmpty(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_FORMAT);
            }

            entireQuestion.answer = entireQuestion.answer.trim().replaceAll(
                    REGEX_EXCEPT_ALPHANUMERIC_COMMA, "");

            String ans = entireQuestion.answer;
            LOGGER.debug("Matching for  answer " + entireQuestion.answer);
            String[] allAns = ans.split(",");

            if (allAns.length == 0) {
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT,
                        "answer not provided in question");
            }

            List<String> mcqANS = new ArrayList<String>();
            for (int i = 0; i < allAns.length; i++) {
                int ansi = 0;

                for (String ansOptn : entireQuestion.formattedOptions.optionOrder) {
                    ansi++;
                    if (StringUtils.equalsIgnoreCase(stripAnsFormat(ansOptn),
                            stripAnsFormat(allAns[i]))) {
                        mcqANS.add("" + ansi + "");
                        break;
                    }
                }
            }

            solutionInfo = new MCQsolutionInfo(entireQuestion.formattedOptions, mcqANS);
        } else if (entireQuestion.type == QuestionType.TEXT || entireQuestion.type == QuestionType.SUBJECTIVE) {
            if (StringUtils.isEmpty(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_QUESTION_FORMAT);
            }

            solutionInfo = new TextSolutionInfo(entireQuestion.formattedOptions,
                    entireQuestion.answer);
        } else if (entireQuestion.type == QuestionType.NUMERIC) {
            if (StringUtils.isEmpty(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT,"Answer not provided");
            }

            entireQuestion.answer = entireQuestion.answer.trim().replaceAll(
                    REGEX_EXCEPT_ALPHANUMERIC_COMMA, "");

            if (!NumberUtils.isNumber(entireQuestion.answer)) {
                throw new VedantuException(VedantuErrorCode.INVALID_ANSWER_FORMAT,"Answer given is not valid");
            }

            solutionInfo = new NumericSolutionInfo(entireQuestion.formattedOptions,
                    entireQuestion.answer);
        }
        if (solutionInfo != null) {
            LOGGER.info("entire question solutions : " + entireQuestion.formattedSolutions);
            solutionInfo.solutions = CollectionUtils.isEmpty(entireQuestion.formattedSolutions) ? new ArrayList<SolutionFormat>()
                    : entireQuestion.formattedSolutions;
        }
        if (metadata == null) {
            LOGGER.debug("No question metadata found User metadata from internal ");
            metadata = getMetadata(entireQuestion.metadata);
        }
        CMDSQuestion qrQuestion = new CMDSQuestion(entireQuestion.formattedQuestion,
                entireQuestion.type, solutionInfo, userId, questionSetName, "", "INCOMPLETE",
                metadata);

        qrQuestion.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        // if (StringUtils.isNotEmpty(metadata.organizationId)) {
        // qrQuestion.contentSrc = new SrcEntity(EntityType.ORGANIZATION,
        // metadata.organizationId);
        // }
        // adding hints here
        if (qrQuestion.hints == null) {
            qrQuestion.hints = new HintInfo();
        }
        qrQuestion.hints.hints = entireQuestion.formattedHints;
        qrQuestion.difficulty = entireQuestion.metadata != null ? entireQuestion.metadata.difficulty
                : Difficulty.UNKNOWN;
        return qrQuestion;
    }

    public static Metadata getMetadata(ParsedQuestionMetadata questionParseMetadata)
            throws VedantuException {

        Metadata metadata = new Metadata();
        return getMetadata(questionParseMetadata, metadata, true);
    }

    public static Metadata getMetadata(ParsedQuestionMetadata questionParseMetadata,
            Metadata metadata, boolean required) throws VedantuException {

        LOGGER.info("validating parse metadata : " + questionParseMetadata);
        MetadataValidator metadataValidator = new MetadataValidator();
        if (!metadataValidator.validate(questionParseMetadata, metadata, required)) {
            throw new VedantuException(VedantuErrorCode.INVALID_METADATA,
                    metadataValidator.getErrorMessage());
        }
        LOGGER.info("returning metadata :" + metadata);
        return metadata;
    }

    public static class InvalidMetadataException extends Exception {

        public InvalidMetadataException(String message, Throwable t) {

            super(message, t);
        }

        public InvalidMetadataException(String message) {

            super(message);
        }

    }

    public static void main(String[] a) {

        String regex = "\\{\\\\rmf\\}";
        String regex1 = "^([\\s]*)([(]?)([a-zA-Z0-9]{1,3})([)\\.]*)";
        System.out.println("<br>shanakr</b>".trim().replaceAll("<[^>]*>", "") + regex + ", "
                + regex1);
        List<String> z = new ArrayList<String>();
        z.add("hello");
        z.add("testing");
        System.out.println(z);
        String p = z.get(z.size() - 1);
        p += "minus";
        z.set(z.size() - 1, p);
        System.out.println(z);
    }

    public static String stripAnsFormat(String ans) {

        return ans.trim().replace("(", "").replace(")", "").replace(".", "");
    }
}