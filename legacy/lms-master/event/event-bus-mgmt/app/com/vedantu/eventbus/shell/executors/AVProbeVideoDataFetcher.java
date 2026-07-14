package com.vedantu.eventbus.shell.executors;

import java.io.BufferedInputStream;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.media.AudioPresets;
import com.vedantu.commons.entity.media.VideoPresets;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.utils.OptionValue;
import com.vedantu.commons.utils.ShellExecutor;

public class AVProbeVideoDataFetcher extends ShellExecutor {

    public enum AVCONVVideoFilePresets {
        WIDTH("width"),
        HEIGHT("height"),
        DISPLAY_ASPECT_RATIO("display_aspect_ratio"),
        DURATION("duration"),
        SAMPLE_RATE("sample_rate"),
        FRAME_RATE("r_frame_rate"),
        AVG_FRAME_RATE("avg_frame_rate"),
        CODEC_TYPE("codec_type"),
        CODEC_NAME("codec_name"),
        UNKNOWN(null);

        private String value;

        AVCONVVideoFilePresets(String value) {

            this.value = value;
        }

        public String getValue() {

            return value;
        }

        public static AVCONVVideoFilePresets valueOfKey(String presetTypeString) {

            AVCONVVideoFilePresets presetType = UNKNOWN;
            try {
                presetType = AVCONVVideoFilePresets.valueOf(presetTypeString);
            } catch (Exception e) {
                LOGGER.error(e.getLocalizedMessage());
            }
            return presetType;
        }

        public static AVCONVVideoFilePresets valueFromPreset(String presetType) {

            for (AVCONVVideoFilePresets preset : values()) {
                if (preset.getValue() != null && preset.getValue().equals(presetType)) {
                    return preset;
                }
            }

            return UNKNOWN;

        }

    }

    String                     keyValuePattern = "\\w+=.*";

    private static ALogger     LOGGER          = Logger.of(AVProbeVideoDataFetcher.class);
    public static final String SHOW_STREAMS    = "-show_streams";
    public static final String SHOW_FORMAT     = "-show_format";

    public static final String STREAM_BLOCK    = "STREAM";
    public static final String FORMAT_BLOCK    = "FORMAT";

    private VideoPresets       videoPresets;
    private AudioPresets       audioPresets;

    public AVProbeVideoDataFetcher() {

        super("avprobe");
        videoPresets = new VideoPresets();
        audioPresets = new AudioPresets();
        monitorable = false;
    }

    public void fetchInfo(File inputVideoFile) throws VedantuException {
        LOGGER.debug("::::::::::::        Inside fetchInfo");
        fetchContainerInfo(inputVideoFile);
        fetchStreamInfo(inputVideoFile);

    }

    private void fetchContainerInfo(File inputVideoFile) throws VedantuException {
        LOGGER.debug("::::::::::::        Inside fetchContainerInfo");
        options.clear();
        OptionValue showStream = new OptionValue();
        showStream.option = SHOW_FORMAT;
        showStream.value = inputVideoFile.getAbsolutePath();
        options.add(showStream);
        execute();
        parseExecutionStream();
        parseErrorStream(inputVideoFile.getAbsolutePath());

    }

    private void fetchStreamInfo(File inputVideoFile) throws VedantuException {
        LOGGER.debug("::::::::::::        Inside fetchStreamInfo");
        options.clear();
        OptionValue showStream = new OptionValue();
        showStream.option = SHOW_STREAMS;
        showStream.value = inputVideoFile.getAbsolutePath();
        options.add(showStream);
        execute();
        parseExecutionStream();
        parseErrorStream(inputVideoFile.getAbsolutePath());

    }

    private void parseExecutionStream() {
        LOGGER.debug("::::::::::::        Inside parseExecutionStream");
        if (this.executionStream != null) {
            Map<AVCONVVideoFilePresets, String> keyValuePatternMap = new HashMap<AVCONVVideoFilePresets, String>();
            LOGGER.debug("Execution log start: ");
            Scanner s = new Scanner(new BufferedInputStream(this.executionStream));
            while (s.hasNextLine()) {

                String nextLine = s.nextLine().trim();
                if (nextLine.matches(keyValuePattern)) {
                    LOGGER.debug("nextLine is "+nextLine);
                    String[] args = nextLine.split("=");
                    if(args.length >= 2){
                        LOGGER.error("Execution Stream " + args[0] + " " + args[1]);
                        if (AVCONVVideoFilePresets.valueFromPreset(args[0]) != AVCONVVideoFilePresets.UNKNOWN) {

                            keyValuePatternMap.put(AVCONVVideoFilePresets.valueFromPreset(args[0]),
                                    args[1].trim());

                        }
                    }
                } else {
                    if (CollectionUtils.isNotEmpty(keyValuePatternMap.keySet())) {
                        if (keyValuePatternMap.containsKey(AVCONVVideoFilePresets.CODEC_TYPE)) {

                            if (keyValuePatternMap.get(AVCONVVideoFilePresets.CODEC_TYPE)
                                    .toLowerCase().equals(MediaType.VIDEO.name().toLowerCase())) {
                                setVideoPreset(keyValuePatternMap);
                            } else if (keyValuePatternMap.get(AVCONVVideoFilePresets.CODEC_TYPE)
                                    .toLowerCase().equals(MediaType.AUDIO.name().toLowerCase())) {
                                setAudioPreset(keyValuePatternMap);
                            }

                        } else {
                            setVideoPreset(keyValuePatternMap);
                            setAudioPreset(keyValuePatternMap);
                        }

                        keyValuePatternMap.clear();
                    }
                }
            }

            LOGGER.debug("Execution log end ");
        }
    }

    private void parseErrorStream(String absolutePath) throws VedantuException {

        if (this.errorStream != null) {
            LOGGER.error("Error Log start ");
            boolean errorOccurred = false;
            try {

                LOGGER.error(" Non empty error stream so error has occured");
                Scanner s = new Scanner(new BufferedInputStream(this.errorStream));
                while (s.hasNextLine()) {
                    String nextLine = s.nextLine();

                    LOGGER.error(nextLine);
                    errorOccurred |= true;
                    break;
                }

            } catch (Exception e) {
                LOGGER.error(" Error while converting video occured ", e);

            } finally {
                if (errorOccurred) {
                    LOGGER.error(" Throwing error ");
                    throw new VedantuException(VedantuErrorCode.VIDEO_INFO_NOT_EXTRACTED,
                            "Video info for " + absolutePath + " can be retrieved");
                }
            }

        }

        if (getExitValue() != 0) {
            LOGGER.error("Info fetch failed ");
            throw new VedantuException(VedantuErrorCode.VIDEO_INFO_NOT_EXTRACTED, "Video info for "
                    + absolutePath + " can be retrieved");

        }
    }

    private void setVideoPreset(Map<AVCONVVideoFilePresets, String> streamMap) {

        for (AVCONVVideoFilePresets key : streamMap.keySet()) {
            LOGGER.debug(" Key " + key + " value " + streamMap.get(key));
            switch (key) {

            case AVG_FRAME_RATE: {

                String[] args = streamMap.get(key).split("/");
                String numeratorString = args[0].trim();
                String denominatorString = args[1].trim();
                LOGGER.error(" Args " + numeratorString + " " + denominatorString);
                if (NumberUtils.isNumber(numeratorString)
                        && NumberUtils.isNumber(denominatorString)) {
                    int numerator = Integer.parseInt(numeratorString);
                    int denominator = Integer.parseInt(denominatorString);
                    if (denominator > 0) {
                        videoPresets.framerate = numerator / denominator;
                    }
                }

                break;

            }
            case WIDTH: {
                if (NumberUtils.isNumber(streamMap.get(key))) {
                    videoPresets.width = Integer.parseInt(streamMap.get(key));
                }
                break;
            }
            case HEIGHT: {
                if (NumberUtils.isNumber(streamMap.get(key))) {
                    videoPresets.height = Integer.parseInt(streamMap.get(key));
                }
                break;
            }
            case DURATION: {
                if (NumberUtils.isNumber(streamMap.get(key))) {
                    videoPresets.duration = Double.parseDouble(streamMap.get(key))*1000;
                }
                break;
            }
            case CODEC_TYPE: {
                videoPresets.codecType = streamMap.get(key);
                break;
            }
            case CODEC_NAME: {
                videoPresets.codecName = streamMap.get(key);
                break;
            }
            default:
                break;
            }
        }

    }

    private void setAudioPreset(Map<AVCONVVideoFilePresets, String> streamMap) {

        for (AVCONVVideoFilePresets key : streamMap.keySet()) {
            switch (key) {
                case SAMPLE_RATE: {
                    if (NumberUtils.isNumber(streamMap.get(key))) {
                        audioPresets.samplerate = (int) Float.parseFloat(streamMap.get(key));
                    }
                    break;
                }
                case CODEC_TYPE: {
                    audioPresets.codecType = streamMap.get(key);
                    break;
                }
                case CODEC_NAME: {
                    videoPresets.codecName = streamMap.get(key);
                    break;
                }
                default:
                    break;
            }
        }

    }

    public VideoPresets getVideoPresets() {

        return videoPresets;
    }

    public AudioPresets getAudioPresets() {

        return audioPresets;
    }

}
