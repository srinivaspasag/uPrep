package com.vedantu.cmds.mgmt.publishers;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.media.AudioPresets;
import com.vedantu.commons.entity.media.VideoPresets;
import com.vedantu.commons.utils.OptionValue;
import com.vedantu.commons.utils.ShellExecutor;

public class AVCONVVideoConverter extends ShellExecutor {

    private static final String TIME                    = "time";
    public static final String  FPS                     = "fps";
    public static final String  FRAME                   = "frame";
    private static final String BITRATE                 = "bitrate";

    private static ALogger      LOGGER                  = Logger.of(AVCONVVideoConverter.class);

    private final String        conversionUpdatePattern = "(\\s*\\w+=\\s*(\\d|\\w|\\/|\\.)+)+";

    private VideoPresets        inputVideoPresets, outputVideoPresets;
    private AudioPresets        inputAudioPresets, outputAudioPresets;

    private int                 completedPercentage;
    private double              totalTime;
    protected int               numberOfThreads;

    public AVCONVVideoConverter() {

        super("avconv");

        setNumberOfThreads(-1);
        setMonitorable(true);
    }

    public boolean convert(File inputVideo, File outputFileVideo) throws VedantuException {

        double completedPercentage = 0;
        if (!inputVideo.exists()) {
            LOGGER.error("File doesn't exist  :" + inputVideo.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputVideo.getAbsolutePath());
        }

        validate(inputVideo);

        if (outputFileVideo.exists()) {
            LOGGER.error("File exist  :" + outputFileVideo.getAbsolutePath() + " so removing file ");
            outputFileVideo.delete();
        }

        OptionValue inputFileOption = new OptionValue();
        inputFileOption.option = "-i";
        inputFileOption.delimeter = EMPTY;
        inputFileOption.value = inputVideo.getAbsolutePath();
        this.options.add(0, inputFileOption);

        OptionValue outputFileOption = new OptionValue();
        outputFileOption.value = outputFileVideo.getAbsolutePath();
        this.options.add(options.size(), outputFileOption);

        double elapsedTimeInProcessedVideo = 0;
        this.execute();

        LOGGER.debug("Converting file now ...");

        if (this.executionStream != null) {

            LOGGER.error("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                LOGGER.error("Execution stream :" + nextLine);
                String statusRow = nextLine.replaceAll("=\\s+", "=");
                if (statusRow.matches(conversionUpdatePattern)) {
                    Map<String, String> valueMap = getMap(statusRow, EMPTY, EQAUL);
                    elapsedTimeInProcessedVideo = Double.parseDouble(valueMap.get(TIME))*1000;

                    if (totalTime != 0) {
                        completedPercentage = (elapsedTimeInProcessedVideo * 100) / totalTime;
                    }

                    LOGGER.error("Completed conversion :" + completedPercentage);
                }

            }

            LOGGER.debug("Execution log end ");
        }

        if (this.errorStream != null) {
            LOGGER.error("Error Log start ");
            boolean flagged = false;
            try {

                LOGGER.error(" Non tempty error stream so error might have occured");
                Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
                while (s.hasNextLine()) {
                    String nextLine = s.nextLine();
                    LOGGER.error(" ErrorStream" + nextLine);
                    flagged = true;
                }

            } catch (Exception e) {
                LOGGER.error(" Error while converting video occured ", e);
                flagged = true;
            } finally {
                if (flagged) {
                    throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                            "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
                }
            }
            completedPercentage = 100;
        }
        LOGGER.error("Error Log end");

        if (getExitValue() != 0) {
            throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                    "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
        }

        return true;

    }

    public boolean grabImage(File inputVideo, File grabImage, String timeOffset)
            throws VedantuException {

        double completedPercentage = 0;
        if (!inputVideo.exists()) {
            LOGGER.error("File doesn't exist  :" + inputVideo.getAbsolutePath());
            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND, "File doesn't exist  :"
                    + inputVideo.getAbsolutePath());
        }

        validate(inputVideo);

        this.options.clear();
        // avconv -ss 15 -i test.mp4 -frames:v 1 test.jpeg
        OptionValue offsetOption = new OptionValue();
        offsetOption.option = "-ss";
        offsetOption.value = timeOffset;
        this.options.add(0, offsetOption);

        OptionValue inputFileOption = new OptionValue();
        inputFileOption.option = "-i";
        inputFileOption.delimeter = EMPTY;
        inputFileOption.value = inputVideo.getAbsolutePath();
        this.options.add(inputFileOption);

        OptionValue frameOptions = new OptionValue();
        frameOptions.option = "-frames:v";
        frameOptions.delimeter = EMPTY;
        frameOptions.value = String.valueOf(1);
        this.options.add(frameOptions);

        OptionValue outputFileOption = new OptionValue();
        outputFileOption.value = grabImage.getAbsolutePath();
        this.options.add(options.size(), outputFileOption);

        double elapsedTimeInProcessedVideo = 0;
        this.execute();

        LOGGER.debug("Can not convert file");

        if (this.executionStream != null) {

            LOGGER.error("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                LOGGER.error("Execution stream :" + nextLine);
                String statusRow = nextLine.replaceAll("=\\s+", "=");
                if (statusRow.matches(conversionUpdatePattern)) {
                    Map<String, String> valueMap = getMap(statusRow, EMPTY, EQAUL);
                    elapsedTimeInProcessedVideo = Double.parseDouble(valueMap.get(TIME));

                    if (totalTime != 0) {
                        completedPercentage = (elapsedTimeInProcessedVideo * 100) / totalTime;
                    }

                    LOGGER.error("Completed conversion :" + completedPercentage);
                }

            }

            LOGGER.debug("Execution log end ");
        }

        if (this.errorStream != null) {
            LOGGER.error("Error Log start ");
            boolean flagged = false;
            try {

                LOGGER.error(" Non tempty error stream so error might have occured");
                Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
                while (s.hasNextLine()) {
                    String nextLine = s.nextLine();
                    LOGGER.error(" ErrorStream" + nextLine);
                    flagged = true;
                }

            } catch (Exception e) {
                LOGGER.error(" Error while converting video occured ", e);
                flagged = true;
            } finally {
                if (flagged) {
                    throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                            "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
                }
            }
            completedPercentage = 100;
        }
        LOGGER.error("Error Log end");

        if (getExitValue() != 0) {
            throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                    "Conversion for video file " + inputVideo.getAbsolutePath() + " failed");
        }

        return true;

    }

    public double getTotalTime() {

        return totalTime;
    }

    public void setTotalTime(double totalTime) {

        this.totalTime = totalTime;
    }

    public Map<String, String> getMap(String input, String delim1, String delim2) {

        Map<String, String> valueMap = new HashMap<String, String>();

        StringTokenizer st = new StringTokenizer(input, delim1);
        while (st.hasMoreTokens()) {
            String[] array = st.nextToken().split(delim2);
            valueMap.put(array[0].trim(), array[1].trim());
        }
        return valueMap;

    }

    public int getNumberOfThreads() {

        return numberOfThreads;
    }

    public void setNumberOfThreads(int numberOfThreads) {

        if (numberOfThreads != -1) {
            this.numberOfThreads = numberOfThreads;
            OptionValue threadOption = new OptionValue();
            threadOption.option = "-threads"; // thread changes rate
            threadOption.value = String.valueOf(this.numberOfThreads);
            threadOption.delimeter = EMPTY;
            this.options.add(threadOption);
        }
    }

    public int getCompletedPercentage() {

        return completedPercentage;
    }

    public void setCompletedPercentage(int completedPercentage) {

        this.completedPercentage = completedPercentage;
    }

    public AudioPresets getAudioPresets() {

        return outputAudioPresets;
    }

    private void validate(File inputVideo) throws VedantuException {

        if (inputVideoPresets == null || inputAudioPresets != null) {
            AVProbeVideoDataFetcher dataFetcher = new AVProbeVideoDataFetcher();
            dataFetcher.fetchInfo(inputVideo);
            inputVideoPresets = dataFetcher.getVideoPresets();
            inputAudioPresets = dataFetcher.getAudioPresets();
        }

        if (outputVideoPresets != null && inputVideoPresets != null) {
            if ((outputVideoPresets.height != -1 && inputVideoPresets.height > outputVideoPresets.height)
                    || (outputVideoPresets.height != -1 && inputVideoPresets.width > outputVideoPresets.width)) {
                throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                        "Illegal height width presets");

            }

            if (outputVideoPresets.framerate != -1
                    && inputVideoPresets.framerate < outputVideoPresets.framerate) {

                throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                        " Illegal framerate  presets " + outputVideoPresets.framerate);

            }
        }
        if (outputAudioPresets != null && inputAudioPresets != null) {
            if (outputAudioPresets.samplerate != -1
                    && inputAudioPresets.samplerate < outputAudioPresets.samplerate) {
                throw new VedantuException(VedantuErrorCode.CONVERSION_FAILED,
                        " Illegal audio sample rate presets");
            }
        }

        this.totalTime = inputVideoPresets.duration;

    }

    public VideoPresets getInputVideoPresets() {

        return inputVideoPresets;
    }

    public void setInputVideoPresets(VideoPresets inputVideoPresets) {

        this.inputVideoPresets = inputVideoPresets;
    }

    public VideoPresets getOutputVideoPresets() {

        return outputVideoPresets;
    }

    public void setOutputVideoPresets(VideoPresets outputVideoPresets) {

        if (outputVideoPresets != null) {

            if (outputVideoPresets.framerate != -1) {
                OptionValue presetOptions = new OptionValue();
                presetOptions.option = "-r:v"; // frame rate
                presetOptions.value = String.valueOf(outputVideoPresets.framerate);
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);
            }

            if (outputVideoPresets.width != -1 && outputVideoPresets.height != -1) {
                OptionValue presetOptions = new OptionValue();
                presetOptions.option = "-s"; // height and width
                presetOptions.value = outputVideoPresets.width + "x" + outputVideoPresets.height;
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);
            }

            if (outputVideoPresets.bitrate == -2) {
                OptionValue presetOptions = new OptionValue();
                presetOptions.option = "-qmin";
                presetOptions.value = "30";
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);

                presetOptions = new OptionValue();
                presetOptions.option = "-qmax";
                presetOptions.value = "50";
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);

                presetOptions = new OptionValue();
                presetOptions.option = "-crf";
                presetOptions.value = "50";
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);
            }


            if (StringUtils.isNotEmpty(outputVideoPresets.fileExt)) {
                OptionValue presetOptions = new OptionValue();
                presetOptions.option = "-f"; // file extension
                presetOptions.value = outputVideoPresets.fileExt;
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);
            }
        }
        this.outputVideoPresets = outputVideoPresets;
    }

    public AudioPresets getInputAudioPresets() {

        return inputAudioPresets;
    }

    public void setInputAudioPresets(AudioPresets inputAudioPresets) {

        this.inputAudioPresets = inputAudioPresets;
    }

    public AudioPresets getOutputAudioPresets() {

        return outputAudioPresets;
    }

    public void setOutputAudioPresets(AudioPresets outputAudioPresets) {

        if (outputAudioPresets != null) {

            if (outputAudioPresets.samplerate != -1) {
                OptionValue presetOptions = new OptionValue();
                presetOptions.option = "-ar"; // sample rate
                presetOptions.value = String.valueOf(outputAudioPresets.samplerate);
                presetOptions.delimeter = EMPTY;
                this.options.add(presetOptions);
            }
        }
        this.outputAudioPresets = outputAudioPresets;
    }

}
