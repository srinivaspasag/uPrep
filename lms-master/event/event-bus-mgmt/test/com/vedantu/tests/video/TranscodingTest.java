package com.vedantu.tests.video;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vedantu.commons.VedantuException;
import com.vedantu.eventbus.shell.executors.AVCONVVideoConverter;

public class TranscodingTest {

    File   validMP4;
    File   invalidMP4;
    File   outputFile;
    File   outputThumbnail;
    String resourceDirectory = System.getProperty("user.dir") + File.separator + "testresources";

    @Before
    public void setUp() {

        validMP4 = new File(resourceDirectory + File.separator + "test.mp4");
        invalidMP4 = new File(resourceDirectory + File.separator + "notavideo.mp4");

        outputFile = new File(resourceDirectory + File.separator + "test.webm");
        outputThumbnail = new File(resourceDirectory + File.separator + "thumbnail.jpg");
    }

    @Before
    public void tearDown() {

        if (outputFile != null && outputFile.exists()) {
            outputFile.delete();
        }

//        if (outputThumbnail != null && outputThumbnail.exists()) {
//            outputThumbnail.delete();
//        }
    }
//
//    @Test
//    public void testVideoConversion() throws VedantuException {
//
//        AVCONVVideoConverter convertor = new AVCONVVideoConverter();
//        convertor.setMonitorable(true);
//        convertor.convert(validMP4, outputFile);
//    }
//
//    //
//    @Test
//    public void testVideoThreaded() throws VedantuException {
//
//        AVCONVVideoConverter convertor = new AVCONVVideoConverter();
//        convertor.setMonitorable(true);
//        convertor.setTotalTime(49.37d);
//        convertor.setNumberOfThreads(2);
//        convertor.convert(validMP4, outputFile);
//    }
//
//    //
//    @Test
//    public void testvideoPresets() {
//
//        AVCONVVideoConverter convertor = new AVCONVVideoConverter();
//        VideoPresets vPresets = new VideoPresets();
//        vPresets.framerate = 10;
//        vPresets.fileExt = "webm";
//        convertor.setOutputVideoPresets(vPresets);
//        AudioPresets aPresets = new AudioPresets();
//
//        aPresets.samplerate = 44100;
//        convertor.setOutputAudioPresets(aPresets);
//        convertor.setMonitorable(true);
//        try {
//            convertor.convert(validMP4, outputFile);
//            AVProbeVideoDataFetcher dataFetcher = new AVProbeVideoDataFetcher();
//            dataFetcher.fetchInfo(validMP4);
//            Assert.assertEquals("video", dataFetcher.getVideoPresets().codecType);
//            Assert.assertEquals(vPresets.framerate, dataFetcher.getVideoPresets().framerate);
//            Assert.assertEquals(aPresets.samplerate, dataFetcher.getAudioPresets().samplerate);
//        } catch (VedantuException e) {
//            Assert.fail(e.errorCode + " " + e.getLocalizedMessage() + "  " + e.getMessage());
//
//        }
//
//    }
//
//    @Test
//    public void testInvalidVideo() {
//
//        AVCONVVideoConverter convertor = new AVCONVVideoConverter();
//        convertor.setMonitorable(true);
//        try {
//            convertor.convert(invalidMP4, outputFile);
//            Assert.fail();
//        } catch (VedantuException e) {
//            Assert.assertEquals(VedantuErrorCode.CONVERSION_FAILED, e.errorCode);
//
//        }
//
//    }

    @Test
    public void testThumbnailGrabber() {

        AVCONVVideoConverter convertor = new AVCONVVideoConverter();
        convertor.setMonitorable(false);
        try {
            convertor.grabImage(validMP4, outputThumbnail, 10);
            Assert.assertTrue(outputThumbnail.exists());
            Assert.assertTrue(outputThumbnail.length() != 0);

        } catch (VedantuException e) {
            Assert.fail(e.errorCode + " " + e.getLocalizedMessage() + "  " + e.getMessage());

        }

    }
}
