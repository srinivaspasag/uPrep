package com.vedantu.tests.video;

import java.io.File;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.eventbus.shell.executors.AVProbeVideoDataFetcher;

public class AVProbeInfoTest {

    File   validMP4;
    File   invalidMP4;
    File   outputFile;
    String resourceDirectory = System.getProperty("user.dir") + File.separator + "testresources";

    @Before
    public void setUp() {

        validMP4 = new File(resourceDirectory + File.separator + "test.mp4");
        invalidMP4 = new File(resourceDirectory + File.separator + "notavideo.mp4");
    }

    @Test
    public void getVideoInfo() {

        AVProbeVideoDataFetcher infoFetcher = new AVProbeVideoDataFetcher();
        try {
            infoFetcher.fetchInfo(validMP4);

        } catch (VedantuException e) {
            Assert.fail("exception occured" + e.getMessage());

        }
        Assert.assertEquals("video", infoFetcher.getVideoPresets().codecType);
        Assert.assertTrue(infoFetcher.getVideoPresets().duration != -1);
        Assert.assertTrue(-1 != infoFetcher.getAudioPresets().samplerate);

    }

    @Test
    public void getInvalidVideoInfo() {

        AVProbeVideoDataFetcher infoFetcher = new AVProbeVideoDataFetcher();
        try {
            infoFetcher.fetchInfo(validMP4);

        } catch (VedantuException e) {

            Assert.assertEquals(VedantuErrorCode.VIDEO_INFO_NOT_EXTRACTED, e.errorCode);

        }

    }
}
