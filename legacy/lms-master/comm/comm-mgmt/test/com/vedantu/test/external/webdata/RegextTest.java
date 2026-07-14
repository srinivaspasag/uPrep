package com.vedantu.test.external.webdata;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.junit.Test;

public class RegextTest {

    @Test
    public void testYoutubeInfo() {

        String inputString = new String(
                "http://www.youtube.com/v/EqdKqnNLs80?version=3&f=videos&app=youtube_gdata");
        Pattern p = Pattern.compile(".*\\/v\\/(.*)\\?.*");
        Matcher m = p.matcher(inputString);

        if (m.matches()) {
            Assert.assertEquals("EqdKqnNLs80", m.group(1));

        }
        String invalidInput = new String(
                "http://www.youtube.com/embed/EqdKqnNLs80?feature=player_embedded");
        m = p.matcher(invalidInput);
        Assert.assertFalse(m.matches());

    }
}
