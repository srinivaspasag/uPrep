package com.vedantu.commons.utils.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vedantu.commons.utils.FileMaskProcessor;

public class FileMaskProcessorTest {

    public static final String PASS_PHRASE       = "a quick brown fox jumps over a lazy dog";
    public static final int    PASS_PHRASE_SIZE  = 256;
    public static byte[]       content;

    public String              inputFileName     = "test.file";
    public String              encryptedFileName = "test_encrypted.file";
    public String              decryptedFileName = "test_decrypted.file";

    @Before
    public void setUp() throws Exception {

        File testFile = new File(inputFileName);
        FileOutputStream stream = new FileOutputStream(testFile);
        content = new byte[PASS_PHRASE_SIZE * 10];
        Random randomBytest = new Random();
        randomBytest.nextBytes(content);
        stream.write(content);
        stream.close();

    }

    @After
    public void tearDown() throws Exception {

        File testFile = new File(inputFileName);
        if (testFile.exists()) {
            testFile.deleteOnExit();
        }
        testFile = new File(encryptedFileName);
        if (testFile.exists()) {
            testFile.deleteOnExit();
        }
        testFile = new File(decryptedFileName);
        if (testFile.exists()) {
            testFile.deleteOnExit();
        }

    }

    @Test
    public void testEncrypt() throws Exception {

        FileMaskProcessor processor = new FileMaskProcessor(PASS_PHRASE, PASS_PHRASE_SIZE);
        byte[] result = new byte[content.length];
        byte[] verificationArray = new byte[content.length];
        processor.process(content, 0, content.length, result);

        processor.process(result, 0, result.length, verificationArray);
        Assert.assertArrayEquals(content, verificationArray);
    }

    @Test
    public void testEncryptFile() throws Exception {

        File testFile = new File("test.file");
        File encryptedFile = new File(encryptedFileName);
        File descryptedFile = new File(decryptedFileName);

        FileMaskProcessor processor = new FileMaskProcessor(PASS_PHRASE, PASS_PHRASE_SIZE);

        processor.process(testFile, PASS_PHRASE_SIZE, encryptedFile);

        processor.process(encryptedFile, PASS_PHRASE_SIZE, descryptedFile);

        FileInputStream inputFile = new FileInputStream(testFile);
        byte[] verificationArray = new byte[content.length];

        inputFile.read(verificationArray);
        inputFile.close();
        Assert.assertArrayEquals(content, verificationArray);

    }

 

}
