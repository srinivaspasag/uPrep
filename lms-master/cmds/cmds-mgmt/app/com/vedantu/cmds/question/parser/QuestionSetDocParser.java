package com.vedantu.cmds.question.parser;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.wmf.tosvg.WMFTranscoder;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xwpf.usermodel.Document;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.VerticalAlign;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFPicture;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSym;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.cmds.constants.QuestionSetFileConstants;
import com.vedantu.cmds.models.CMDSQuestion;
import com.vedantu.cmds.pojos.content.question.EntireQuestion;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.content.pojos.tests.Metadata;

public class QuestionSetDocParser {

    public static final String   TEMP_QUESTION_IMAGES_DIR = "cmds";
    private static final ALogger LOGGER                   = Logger.of(QuestionSetDocParser.class);
    static SymbolMap             symbols;

    static {
        try {
            symbols = new SymbolMap();
        } catch (FileNotFoundException e) {
            LOGGER.error("Error in reading file" + e.getMessage());
        }
    }
    private static final String  UNICODE_REGEX            = "([\\x20-\\xFE]*)([^\\x20-\\xFE]?)([\\x20-\\xFE]*)";
    private static final String  PRINTABLE_UNICODE_REGEX  = "([\\x20-\\xFE]?)";
    static final Pattern         p                        = Pattern
                                                                  .compile(
                                                                          UNICODE_REGEX,
                                                                          Pattern.UNICODE_CASE
                                                                                  | Pattern.CASE_INSENSITIVE);
    static final Pattern         printablePattern         = Pattern
                                                                  .compile(
                                                                          PRINTABLE_UNICODE_REGEX,
                                                                          Pattern.UNICODE_CASE
                                                                                  | Pattern.CASE_INSENSITIVE);

    public static List<CMDSQuestion> getContent(String orgId, String providedQuestionSetFileName,
            File file, String userId, Metadata metadata) throws Exception, IOException,
            VedantuException {

        List<EntireQuestion> allQuestions = new ArrayList<EntireQuestion>();

        XWPFDocument doc = new XWPFDocument(new FileInputStream(file));
        List<CMDSQuestion> questions = new ArrayList<CMDSQuestion>();
        EntireQuestion entireQuestion = null;
        boolean seenQuestion = false;

        ParsedQuestionMetadata globalParseMetadata = null;
        ParsedQuestionMetadata questionParseMetadata = null;
        //
        int i = 0;
        for (XWPFParagraph para : doc.getParagraphs()) {
            i++;
            metadata.__setErrorLine("error on paraNo : " + i + " and para text : "
                    + para.getParagraphText() + " ,  IndentationFirstLine: "
                    + para.getIndentationFirstLine());
            boolean newPara = true;
            String htmlText = "";
            List<String> uuids = new ArrayList<String>(); // Holds uuid
            for (XWPFRun run : para.getRuns()) {
                htmlText += annotateHtmlStyle(run);
                if (null != run.getEmbeddedPictures()) {
                    for (XWPFPicture p : run.getEmbeddedPictures()) {

                        String imgType = null;
                        byte[] imgBytes = p.getPictureData().getData();
                        if (Document.PICTURE_TYPE_JPEG == p.getPictureData().getPictureType()
                                || Document.PICTURE_TYPE_PNG == p.getPictureData().getPictureType()) {
                            imgType = Document.PICTURE_TYPE_PNG == p.getPictureData()
                                    .getPictureType() ? "png" : "jpg";
                        } else if (Document.PICTURE_TYPE_WMF == p.getPictureData().getPictureType()) {
                            WMFTranscoder wmfTranscoder = new WMFTranscoder();
                            wmfTranscoder.addTranscodingHint(WMFTranscoder.KEY_HEIGHT, new Float(
                                    100));
                            wmfTranscoder.addTranscodingHint(WMFTranscoder.KEY_WIDTH,
                                    new Float(100));
                            TranscoderInput wmfTranscoderInput = new TranscoderInput(
                                    new ByteArrayInputStream(imgBytes));
                            ByteArrayOutputStream svgBAOS = new ByteArrayOutputStream();
                            TranscoderOutput wmfTranscoderOutput = new TranscoderOutput(svgBAOS);
                            wmfTranscoder.transcode(wmfTranscoderInput, wmfTranscoderOutput);

                            JPEGTranscoder jpegTranscoder = new JPEGTranscoder();
                            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_HEIGHT, new Float(
                                    100));
                            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_WIDTH, new Float(
                                    100));
                            jpegTranscoder.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,
                                    new Float(0.8));
                            TranscoderInput jpegInput = new TranscoderInput(
                                    new ByteArrayInputStream(svgBAOS.toByteArray()));
                            ByteArrayOutputStream jpegBAOS = new ByteArrayOutputStream();
                            TranscoderOutput jpegOutput = new TranscoderOutput(jpegBAOS);
                            jpegTranscoder.transcode(jpegInput, jpegOutput);

                            imgBytes = jpegBAOS.toByteArray();
                            imgType = "jpg";

                        } else {
                            LOGGER.info("GET UNICODE CHARACTERS HERE else: " + run.toString());
                        }

                        if (null == imgType) {
                            throw new Exception("unsupported image type : "
                                    + p.getPictureData().getPictureType() + " for "
                                    + p.getPictureData().getFileName());
                        }

                        //
                        // LOGGER.info("\nfile extension : " +
                        // p.getPictureData().suggestFileExtension() +
                        // ", type : " + p.getPictureData().getPictureType());
                        //

                        String randomUUID = UUID.randomUUID().toString();
                        uuids.add(randomUUID);
                        LOGGER.debug("random uuid" + randomUUID);

                        File trgetFile = FileSystemFactory.INSTANCE.getTempFS()
                                .getFileWithSpecifiedName(TEMP_QUESTION_IMAGES_DIR, randomUUID,
                                        "jpg");

                        LOGGER.debug("target file name::::" + trgetFile.getAbsolutePath());

                        InputStream in = new ByteArrayInputStream(p.getPictureData().getData());
                        LOGGER.debug(" ValidInputstream" + in.available());

                        BufferedImage bImageFromConvert = ImageIO.read(in);
                        //ImageIO.write(bImageFromConvert, "jpg", trgetFile);
                        BufferedImage imageRGB = new BufferedImage(bImageFromConvert.getWidth(), bImageFromConvert.getHeight(), BufferedImage.TYPE_INT_RGB);
                        imageRGB.createGraphics().drawImage(bImageFromConvert, null, null);
                        ImageIO.write(imageRGB, "jpg", trgetFile);

                        // htmlText += "<img  src=\""
                        // + HadoopDisplayURLs
                        // .getQuestionEmbedTempImage(randomUUID + "."
                        // + imgType) + "\" />";
                        // htmlText += randomUUID;
                        LOGGER.info("input image type : " + imgType
                                + ", and image will be saved as jpg");
                        htmlText += ImageDisplayURLUtil.getEmbededHtml(
                                getTempImageURL(randomUUID
                                        + FileUtils.JPG_EXTENTION), randomUUID);
                        in.close();
                    }
                }
            }

            // read metadata here
            LOGGER.info("Organization id : " + orgId);
            if ((htmlText.trim().toLowerCase().startsWith(QuestionSetFileConstants.PREFIX_TITLE) || htmlText
                    .trim().toLowerCase().startsWith(QuestionSetFileConstants.PREFIX_INSTITUTE))
                    && globalParseMetadata == null) {
                LOGGER.info("Creating for global metadata : ");
                globalParseMetadata = new ParsedQuestionMetadata(orgId);

            }
            if (globalParseMetadata != null && !seenQuestion) {
                LOGGER.info("parsing for global metadata : ");

                globalParseMetadata.accumulateMetadataInfo(htmlText, false);
            }
            LOGGER.info("previous entire question object : " + entireQuestion);
            LOGGER.info("html text : " + htmlText);
            if (htmlText.trim().toLowerCase().startsWith(QuestionSetFileConstants.PREFIX_QUESTION)) {
                seenQuestion = true;
                LOGGER.info("previous entire question object : " + entireQuestion);
                entireQuestion = new EntireQuestion(orgId);
                if (globalParseMetadata != null) {
                    LOGGER.info("Cloning  global metadata : ");
                    questionParseMetadata = globalParseMetadata.clone();

                    LOGGER.info("Original  global metadata : " + globalParseMetadata);
                    LOGGER.info("cloned  global metadata : " + questionParseMetadata);

                }
                allQuestions.add(entireQuestion);
            }
            if (null != entireQuestion) {
                LOGGER.info("accumulating question data");
                entireQuestion.accumulateQuestionInfo(seenQuestion, htmlText.trim(), htmlText,
                        uuids, false, false, newPara);
                // this will parse the local metadata
                if (questionParseMetadata != null) {
                    LOGGER.info("Parsing question metadata : ");
                    questionParseMetadata.accumulateMetadataInfo(htmlText, true);
                } else {
                    LOGGER.info("No question metadata for : " + entireQuestion);
                }
                entireQuestion.metadata = questionParseMetadata;
            }
            LOGGER.info("after accumulation entire question object : " + entireQuestion);
        }

        for (int questionIndexInQuestionSet = 0; questionIndexInQuestionSet < allQuestions.size(); questionIndexInQuestionSet++) {
            try {
                EntireQuestion eq = allQuestions.get(questionIndexInQuestionSet);

                String questionSetFileName = providedQuestionSetFileName;
                if (StringUtils.isEmpty(questionSetFileName)) {
                    questionSetFileName = file.getName();
                }

                CMDSQuestion question = eq.toQuestion(eq, orgId, userId, questionSetFileName, null);

                if (question != null) {
                    question.addHook();
                    questions.add(question);
                }
            } catch (VedantuException exception) {
                throw new VedantuException(exception.errorCode, "For Question "
                        + (questionIndexInQuestionSet + 1) + ": " + exception.getMessage());
            }

        }
        if (globalParseMetadata != null) {
            metadata = EntireQuestion.getMetadata(globalParseMetadata, metadata, false);
            LOGGER.info("global metadata is : " + metadata);
        }
        return questions;

    }

    public static String getTempImageURL(String image) {

        return ImageDisplayURLUtil.getImgHost() + "/temp/cmds/" + image;
    }

    private static final String SPACE_UNICODE = "\\uF020";

    private static String annotateHtmlStyle(XWPFRun run) {

        String htmlRunText = "";
        LOGGER.info("<span style=\"font-family:" + run.getFontFamily() + ";\">" + "run string: "
                + run.toString());
        if (run.getFontFamily() != null) {
            String rnTxt = run.toString();
            if (StringUtils.isEmpty(rnTxt)) {
                rnTxt = getRunTextFromDOM(run);
            }
            if (rnTxt == null) {
                rnTxt = "";
            }
            String unscapeString = rnTxt.replaceAll(SPACE_UNICODE, "").replaceAll(" ", "");
            boolean hasSpace = (run.toString().length() != unscapeString.length());
            try {

                if (symbols.symbol.containsKey(StringEscapeUtils.unescapeJava(unscapeString))) {
                    htmlRunText = StringEscapeUtils.unescapeJava(symbols.symbol.get(unscapeString));
                    htmlRunText += hasSpace ? " " : "";
                } else {
                    StringBuilder sb = new StringBuilder();
                    Matcher m = p.matcher(run.toString());
                    while (m.find()) {
                        sb.append(m.group(1))
                                .append(StringUtils.isNotEmpty(m.group(2))
                                        && symbols.symbol.containsKey(StringEscapeUtils
                                                .unescapeJava(m.group(2))) ? StringEscapeUtils
                                        .unescapeJava(symbols.symbol.get(m.group(2))) : m.group(2))
                                .append(m.group(3));
                    }
                    htmlRunText += sb.toString();
                }
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            String rnTxt = run.toString();
            if (StringUtils.isEmpty(rnTxt)) {
                rnTxt = getRunTextFromDOM(run);
            }
            if (rnTxt == null) {
                rnTxt = "";
            }
            StringBuilder sb = new StringBuilder();
            LOGGER.info("is run text empty: " + StringUtils.isEmpty(run.toString()));
            Matcher m = p.matcher(rnTxt);
            while (m.find()) {
                sb.append(m.group(1))
                        .append(StringUtils.isNotEmpty(m.group(2))
                                && symbols.symbol.containsKey(StringEscapeUtils.unescapeJava(m
                                        .group(2))) ? StringEscapeUtils.unescapeJava(symbols.symbol
                                .get(m.group(2))) : m.group(2)).append(m.group(3));
            }
            htmlRunText += sb.toString();
            LOGGER.info("htmlRunText after symbol mapping  : " + htmlRunText);
        }
        String matchString = htmlRunText.trim().toLowerCase();
        if (matchString.startsWith("options:") || matchString.startsWith("type:")
                || matchString.startsWith("answer:") || matchString.startsWith("question")
                || matchString.startsWith("columna:") || matchString.startsWith("columnb:")) {
            // do nothing
        } else {
            VerticalAlign verticalAlign = run.getSubscript();
            if (VerticalAlign.BASELINE == verticalAlign) {
                // TODO: Fix this by maintaining global/previous baseline
                int textPosWrtBaseline = run.getTextPosition() + VerticalAlign.BASELINE.getValue();
                verticalAlign = (textPosWrtBaseline > 0) ? VerticalAlign.SUPERSCRIPT
                        : (textPosWrtBaseline < 0 ? VerticalAlign.SUBSCRIPT
                                : VerticalAlign.BASELINE);
            }
            switch (verticalAlign) {
            case SUBSCRIPT:
                htmlRunText = "<sub>" + htmlRunText + "</sub>";
                break;
            case SUPERSCRIPT:
                htmlRunText = "<sup>" + htmlRunText + "</sup>";
                break;
            case BASELINE:
            default:
                break;
            }
            LOGGER.info("run text after alignment mapping: " + htmlRunText);

            if (run.isStrike()) {
                htmlRunText = "<strike>" + htmlRunText + "</strike>";
            }
            if (run.isBold()) {
                htmlRunText = "<b>" + htmlRunText + "</b>";
            }
            if (run.isItalic()) {
                htmlRunText = "<i>" + htmlRunText + "</i>";
            }
            if (run.getUnderline() != UnderlinePatterns.NONE) {
                htmlRunText = "<u>" + htmlRunText + "</u>";
            }
        }
        return htmlRunText;
    }

    private static String getRunTextFromDOM(XWPFRun run) {

        @SuppressWarnings("deprecation")
        CTSym[] ctSym = run.getCTR().getSymArray();
        String rnTxt = null;
        if (ctSym != null) {
            for (CTSym s : ctSym) {
                String str = new String(s.getChar());
                LOGGER.info("symbol string: " + str);
                if (s.getDomNode().hasAttributes()
                        && StringUtils.equalsIgnoreCase(s.getFont(), "Symbol")) {
                    str = s.getDomNode().getAttributes().getNamedItem("w:char").getNodeValue();
                    Matcher m = printablePattern.matcher(str);
                    try {
                        if (m.find()) {
                            str = StringEscapeUtils.unescapeJava("\\uF0" + str.substring(2));
                        } else {
                            str = StringEscapeUtils.unescapeJava("\\u" + str);
                        }
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                rnTxt = str;
            }
        }
        return rnTxt;
    }
}
