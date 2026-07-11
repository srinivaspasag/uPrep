package com.vedantu.commons.utils.test;

import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SecurityCredentials;
import com.vedantu.commons.utils.EncryptionUtils;

public class EncryptionTest {

    public static final String PASS_PHRASE      = "a quick brown fox jumps over a lazy dog";
    public static final int    PASS_PHRASE_SIZE = 256;
    public static byte[]       content;

    @Before
    public void setUp() throws Exception {

        content = new byte[PASS_PHRASE_SIZE * 10];
        Random randomBytest = new Random();
        randomBytest.nextBytes(content);

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Testing single level encryption
     * 
     * @throws Exception
     * @throws VedantuException
     */
    @Test
    public void testEncrypt() throws Exception, VedantuException {

        SecurityCredentials credentials = EncryptionUtils.generateKeys();
        String passPhrase = EncryptionUtils.generatePassphrase();

        String encryptedPP = EncryptionUtils.encryptWithPublicKey(passPhrase,
                credentials.getPublicKey());

        String decreptedPP = EncryptionUtils.decryptWithPrivateKey(encryptedPP,
                credentials.getPrivateKey());

        Assert.assertEquals(passPhrase, decreptedPP);
    }

    /**
     * test two level encryption
     * 
     * @throws Exception
     * @throws VedantuException
     */
    @Test
    public void testTwoLevelEncryption() throws Exception, VedantuException {

        String passPhrase = EncryptionUtils.generatePassphrase();
        SecurityCredentials orgKeys = EncryptionUtils.generateKeys();
        SecurityCredentials userKeys = EncryptionUtils.generateKeys();

        String encryptedPP1 = EncryptionUtils.encryptWithPrivateKey(passPhrase,
                orgKeys.getPrivateKey());

        String encryptedPP2 = EncryptionUtils.encryptWithPublicKey(encryptedPP1,
                userKeys.getPublicKey());

        String decreptedPP2 = EncryptionUtils.decryptWithPrivateKey(encryptedPP2,
                userKeys.getPrivateKey());
        Assert.assertEquals(encryptedPP1, decreptedPP2);

        String decreptedPP1 = EncryptionUtils.decryptWithPublicKey(decreptedPP2,
                orgKeys.getPublicKey());
        Assert.assertEquals(passPhrase, decreptedPP1);

    }

}
