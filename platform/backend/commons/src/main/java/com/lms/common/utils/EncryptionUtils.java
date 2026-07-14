package com.lms.common.utils;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.commons.pojos.requests.SecurityCredentials;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
@Component
public class EncryptionUtils {


        private static   final Logger logger = LoggerFactory.getLogger(EncryptionUtils.class);

        private static final int      MAXIMUM_PASSPHRASE_SIZE = 256;

    public static final String ASCII = "ASCII";

    public static final String UTF_8 = "UTF-8";

        private static final String  RSA_ALGORITHM           = "RSA";

        private static final int      RSA_CHUNK_SIZE          = 117;

        public static String getAsciiByte64Encoding(String data) throws UnsupportedEncodingException {

            return new String(Base64.encodeBase64(data.getBytes(StandardCharsets.UTF_8)), StandardCharsets.US_ASCII);

    }


        public static String generatePassphrase() {

        Random random = new Random();
        byte[] randomizedBytes = new byte[MAXIMUM_PASSPHRASE_SIZE];
        random.nextBytes(randomizedBytes);
            return new String(Base64.encodeBase64(randomizedBytes), StandardCharsets.US_ASCII);

        }

        public static SecurityCredentials generateKeys() throws VedantuException {

        KeyPairGenerator keyGenerator;
        try {
            keyGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);

            keyGenerator.initialize(1024);
            logger.debug("generating new public/private key pairs");
            KeyPair pair = keyGenerator.genKeyPair();
            return new SecurityCredentials(pair.getPrivate().getEncoded(), pair.getPublic()
                    .getEncoded());
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
            throw new VedantuException(VedantuErrorCode.CAN_NOT_GENERATE_SECURITY_CREDENTIALS);
        }
    }

        /**
         * RS
         *
         * @param passPhrase
         * @param encodedPublicKey
         * @return
         */
        public static String encryptWithPublicKey(String passPhrase, byte[] encodedPublicKey) {

        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);

            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey kPublicKey = keyFactory.generatePublic(publicKeySpec);

            byte[] ppByteArr = RSAEncrypt(passPhrase.getBytes(), kPublicKey);
            passPhrase = DatatypeConverter.printHexBinary(ppByteArr);
            return passPhrase;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

        public static String encryptWithPrivateKey(String passPhrase, byte[] encodedPrivateKey) {

        if (encodedPrivateKey == null) {
            return null;
        }
        KeyFactory keyFactory;
        try {
            keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey kPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            byte[] ppByteArr = RSAEncrypt(passPhrase.getBytes(), kPrivateKey);
            passPhrase = DatatypeConverter.printHexBinary(ppByteArr);
            return passPhrase;
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

        public static String decryptWithPublicKey(String passPhrase, byte[] encodedPublicKey) {

        try {

            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey kPublicKey = keyFactory.generatePublic(publicKeySpec);
            byte[] decreptedPassPhrase = RSADecrypt(DatatypeConverter.parseHexBinary(passPhrase),
                    kPublicKey);
            return new String(decreptedPassPhrase);
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

        public static String decryptWithPrivateKey(String passPhrase, byte[] encodedPrivateKey) {

        try {

            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(encodedPrivateKey);
            PrivateKey kPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            byte[] decreptedPassPhrase = RSADecrypt(DatatypeConverter.parseHexBinary(passPhrase),
                    kPrivateKey);
            logger.debug("decrypted key: " + decreptedPassPhrase);
            logger.debug("decrypted string: " + new String(decreptedPassPhrase));
            return new String(decreptedPassPhrase);

        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        } catch (InvalidKeySpecException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchPaddingException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

        public static String getDecryptedPassPhrase(String pp) {

        byte[] privateKey = null;
        try {

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKey);
            PrivateKey kPrivateKey = keyFactory.generatePrivate(privateKeySpec);
            byte[] decryptedKey = RSADecrypt(DatatypeConverter.parseHexBinary(pp), kPrivateKey);
            logger.info("decrypted key: " + decryptedKey);
            logger.info("decrypted string: " + new String(decryptedKey));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public static byte[] RSAEncrypt(byte[] clear, Key key) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return processData(cipher, clear, RSA_CHUNK_SIZE);
    }

    public static byte[] RSADecrypt(byte[] encrypted, Key key) throws Exception {

        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return processData(cipher, encrypted, 128);
    }

        private static byte[] processData(Cipher cipher, byte[] clear, int processChunk)
            throws IllegalBlockSizeException, BadPaddingException {

        byte[] processed = new byte[] {};
        if (clear.length > processChunk) {

            int loopCount = (clear.length % processChunk) == 0 ? (clear.length / processChunk)
                    : ((clear.length / processChunk) + 1);
            for (int i = 0; i < loopCount; i++) {
                int srcPos = i * processChunk;
                System.out.println("i= " + i + " srcPos: " + srcPos);
                int endPos = Math.min(processChunk, (clear.length - srcPos));
                byte[] encPart = new byte[endPos];
                if (i == clear.length / endPos && clear.length % endPos != 0) {
                    endPos = clear.length % endPos;
                }
                System.arraycopy(clear, srcPos, encPart, 0, endPos);
                encPart = processChunkData(cipher, encPart);
                processed = copyBytes(processed, encPart);
                encPart = null;
            }
        } else {
            processed = processChunkData(cipher, clear);
        }
        return processed;
    }

        private static byte[] processChunkData(Cipher cipher, byte[] clear)
            throws IllegalBlockSizeException, BadPaddingException {

        System.out.println("input chunks : " + clear.length);
        byte[] processed = cipher.doFinal(clear);
        System.out.println("output chunks : " + processed.length);
        return processed;
    }

        private static byte[] copyBytes(byte[] one, byte[] two) {

        byte[] combined = new byte[one.length + two.length];
        for (int i = 0; i < combined.length; ++i) {
            combined[i] = i < one.length ? one[i] : two[i - one.length];
        }
        return combined;
    }

        public String getHexString(byte[] bytes) {

        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            result += Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    }

