package com.lms.common.fs.handlers;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.Base16Lower;
import com.lms.common.exception.VedantuException;
import com.lms.common.fs.exception.FileStoreException;
import com.lms.common.fs.handlers.responce.SignUploadFileRes;
import com.lms.common.utils.EncryptionUtils;
import com.lms.common.utils.FileUtils;
import com.lms.common.vedantu.commons.pojos.requests.FileData;
import com.lms.common.vedantu.entity.storage.MediaType;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.StorageIdentification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class S3Handler implements IFileSystemHandler {

    public static final String VEDANTU = "uprep";
    private static final Logger logger = LoggerFactory.getLogger(S3Handler.class);
    private AmazonS3 s3client;
    @Value("${amazon.s3.bucket.identifier}")
    private String bucketName;
    @Value("${amazon.s3.accessKey}")
    private String accessKey;
    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    public static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    public static final int MAXIMUM_EXPIRATION_TIME_OFFSET = 3600 * 1000;
    SimpleDateFormat iso8601formattter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat iso8601formattter2 = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    SimpleDateFormat iso8601formattter3 = new SimpleDateFormat("yyyyMMdd");
    AWSCredentials credentials = null;
    @Value("${amazon.s3.secretKey}")
    private String secretKey;

    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_SHA256_ALGORITHM));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public boolean createParent(String dir) throws VedantuException {
        return false;
    }

    @Override
    public boolean store(File file, String destDir, String destFileName, Map<String, String> tags) throws FileNotFoundException {
        ObjectMetadata metadata = new ObjectMetadata();
        PutObjectRequest request = file.isDirectory() ? new PutObjectRequest(bucketName, destFileName, file)
                : new PutObjectRequest(bucketName, destFileName, new FileInputStream(file), metadata);
        request.setCannedAcl(CannedAccessControlList.Private);
       /*  TransferManager manager = TransferManagerBuilder.standard().withS3Client(s3client).build();
         Transfer myupload = null;
       //s3client.putObject(new PutObjectRequest(bucketName, fileName, file).withCannedAcl(CannedAccessControlList.PublicRead));
         if (file.isDirectory()) {
             myupload = manager.uploadDirectory(request.getBucketName(), request.getKey(),
                     request.getFile(), true);
         } else {
             myupload = manager.upload(request);
         }
         try {
             Thread.sleep(5000);
         } catch (InterruptedException e) {
             logger.debug("Interuppted thread ");
         }
         return myupload.isDone();*/
        s3client.putObject(destDir, destFileName, file);
        return true;

    }

    @Override
    public FileData get(String sourceDir, String srcFileName) throws VedantuException {
        return null;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size) throws VedantuException {
        return null;
    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {
        return entityType._getStorageId().toLowerCase() + FileUtils.SEPARATOR_HYPHEN
                + fwkId.toLowerCase() + FileUtils.SEPARATOR_HYPHEN + bucketName;
    }

    @PostConstruct
    private void initializeAmazon() {
        credentials = new BasicAWSCredentials("__AWS_ACCESS_KEY_REDACTED__", "__AWS_SECRET_KEY_REDACTED__");
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxConnections(500);
        configuration.setProtocol(Protocol.HTTPS);
        configuration.setConnectionTimeout(60000);
        configuration.setMaxErrorRetry(2);
        configuration.setSocketTimeout(60000);
        this.s3client = AmazonS3ClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion("uprep".equalsIgnoreCase("learnpediaqa") || "uprep".equalsIgnoreCase("learnpedialive") ? Regions.US_EAST_1 : Regions.AP_SOUTH_1)
                .withPathStyleAccessEnabled(true).withClientConfiguration(configuration).build();

    }

    @Override
    public boolean exists(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override
    public StorageIdentification getIdentification() throws VedantuException {
        return null;
    }

    @Override
    public boolean removeParent(String dirPath) throws VedantuException {
        return false;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws VedantuException {
        return 0;
    }

    @Override
    public SignUploadFileRes signContentUpload(EntityType entityType, String bucketName, String fileName, String contentType) throws FileStoreException {
        try {
            if (VEDANTU.equalsIgnoreCase("learnpediaqa") || VEDANTU.equalsIgnoreCase("learnpedialive")) {
                // OLD Buckets
                return signContentUploadV2(entityType, bucketName, fileName, contentType);
            } else {
                // New Buckets
                return signContentUploadV4(entityType, bucketName, fileName, contentType);
            }
        } catch (Exception exception) {
            throw new FileStoreException(exception.getMessage());
        }
    }

    @Override
    public FileData getSecureURL(String sourceId, EntityType eType, MediaType mediaType, String fileName) throws FileStoreException {

        if (!s3client.doesBucketExist(sourceId)) {
            logger.debug("Bucket not found :" + sourceId);
            return null;
        }

        java.util.Date expiration = new java.util.Date();
        long milliSeconds = expiration.getTime();
        milliSeconds += 1000 * 60 * 60; // Add 1 hour.
        expiration.setTime(milliSeconds);

        FileData data = new FileData();
        GetObjectMetadataRequest metadataReq = null;
        ObjectMetadata metadata = null;
        try {
            metadataReq = new GetObjectMetadataRequest(sourceId, fileName);
            metadata = s3client.getObjectMetadata(metadataReq);
        } catch (Exception exp) {
            logger.debug("File not found :" + sourceId + "/" + fileName);
            return null;
        }
        data.setContentLength(metadata.getContentLength());
        logger.debug("found content length content size " + metadata.getContentLength());
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                sourceId, fileName);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);

        URL url = s3client.generatePresignedUrl(generatePresignedUrlRequest);

        logger.debug("Pre-Signed URL = " + url.toString());

        data.setSecuredURL(url.toString());
        return data;

    }

    public SignUploadFileRes signContentUploadV2(EntityType type, String bucketName, String fileName,
                                                 String contentType) throws FileStoreException, UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        SignUploadFileRes signedURLRes = new SignUploadFileRes();
        long currentMilliseconds = System.currentTimeMillis();
        String expirationTime = iso8601formattter.format(new Date(currentMilliseconds + MAXIMUM_EXPIRATION_TIME_OFFSET));
        String policyDocument = "{\"expiration\": \"" + expirationTime + "\","
                + "\"conditions\": [" + "{\"bucket\": \"" + bucketName + "\"},"
                + "[\"starts-with\", \"$key\", \"\"]," + "{\"acl\": \"" + "private" + "\"},"
                + "[\"starts-with\", \"$Content-Type\", \"\"]" +
                "]" + "}";
        String policy = EncryptionUtils.getAsciiByte64Encoding(policyDocument);
        signedURLRes.url = "https://s3.amazonaws.com/" + bucketName;
        signedURLRes.requestParams.put("signature", sign(policy));
        signedURLRes.requestParams.put("policy", policy);
        signedURLRes.requestParams.put("AWSAccessKeyId", credentials.getAWSAccessKeyId());
        signedURLRes.requestParams.put("acl", "private");
        signedURLRes.requestParams.put("key", fileName);
        signedURLRes.requestParams.put("Content-Type", contentType);
        signedURLRes.verificationRequired = false;
        logger.debug(" Calculated ContentType " + contentType);
        signedURLRes.contentType = contentType;
        return signedURLRes;
    }

    private String sign(String data)
            throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {

        Mac hmac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        hmac.init(new SecretKeySpec(credentials.getAWSSecretKey().getBytes(StandardCharsets.UTF_8),
                HMAC_SHA1_ALGORITHM));

        return new String(Base64.getEncoder().encode(hmac.doFinal(data.getBytes(StandardCharsets.UTF_8))),
                StandardCharsets.US_ASCII);

    }

    public SignUploadFileRes signContentUploadV4(EntityType type, String bucketName, String fileName,
                                                 String contentType) throws Exception {
        SignUploadFileRes signedURLRes = new SignUploadFileRes();
        long currentMilliseconds = System.currentTimeMillis();
        String expirationTime = iso8601formattter.format(new Date(currentMilliseconds + MAXIMUM_EXPIRATION_TIME_OFFSET));
        Date date = new Date(currentMilliseconds);
        String dateKey = iso8601formattter3.format(date);
        String amzDate = iso8601formattter2.format(date);
        String credentialsString = credentials.getAWSAccessKeyId() + "/" + dateKey + "/" + Regions.AP_SOUTH_1.getName() + "/s3/aws4_request";
        String policyDocument = "{\"expiration\": \"" + expirationTime + "\","
                + "\"conditions\": [" + "{\"bucket\": \"" + bucketName + "\"},"
                + "[\"starts-with\", \"$key\", \"" + fileName + "\"]," + "{\"acl\": \"" + "private" + "\"},"
                + "[\"starts-with\", \"$Content-Type\", \"" + contentType + "\"],"
                + "{\"x-amz-algorithm\": \"" + "AWS4-HMAC-SHA256" + "\"},"
                + "{\"x-amz-credential\": \"" + credentialsString + "\"},"
                + "{\"x-amz-date\": \"" + amzDate + "\"}" +
                "]" + "}";
        String policy = EncryptionUtils.getAsciiByte64Encoding(policyDocument);
        signedURLRes.url = "https://s3." + Regions.AP_SOUTH_1.getName() + ".amazonaws.com/" + bucketName;
        byte[] signingKey = getSignatureKey(credentials.getAWSSecretKey(), dateKey, Regions.AP_SOUTH_1.getName(), "s3");
        signedURLRes.requestParams.put("x-amz-signature", signHex(policy, signingKey));
        signedURLRes.requestParams.put("policy", policy);
        signedURLRes.requestParams.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        signedURLRes.requestParams.put("x-amz-credential", credentialsString);
        signedURLRes.requestParams.put("x-amz-date", amzDate);
//        signedURLRes.requestParams.put("AWSAccessKeyId", awsCredentials.getAWSAccessKeyId());
        signedURLRes.requestParams.put("acl", "private");
        signedURLRes.requestParams.put("key", fileName);
        signedURLRes.requestParams.put("Content-Type", contentType);
        signedURLRes.verificationRequired = false;
        logger.debug(" Calculated ContentType " + contentType);
        signedURLRes.contentType = contentType;
        return signedURLRes;
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    private String signHex(String policy, byte[] signingKey) throws Exception {
        byte[] kDate = HmacSHA256(policy, signingKey);
        return Base16Lower.encodeAsString(kDate);
    }
  /*public static final String  frameworkId = "fwkId";

    public static final int      MAXIMUM_EXPIRATION_TIME_OFFSET   = 3600 * 1000;

    private static final Logger logger = LoggerFactory.getLogger(S3Handler.class);

    //private AWSCredentials       awsCredentials                   = null;
    // Time format iso-8601-format
    SimpleDateFormat iso8601formattter                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat             iso8601formattter2                = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    SimpleDateFormat             iso8601formattter3                = new SimpleDateFormat("yyyyMMdd");

    public static final String   HMAC_SHA1_ALGORITHM              = "HmacSHA1";
    public static final String   HMAC_SHA256_ALGORITHM            = "HmacSHA256";
    // http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html
//    private static final String  AMAZON_S3_ERROR_CODE_NO_SUCH_KEY = "NoSuchKey";
   // private AmazonS3             client                           = null;

    public S3Handler() {

        awsCredentials = new BasicAWSCredentials(AmazonS3Manager.get().accessKeyId,
                AmazonS3Manager.get().secretAccessKey);
        ClientConfiguration configuration = new ClientConfiguration();
        configuration.setMaxConnections(500);
        configuration.setProtocol(Protocol.HTTPS);
        configuration.setConnectionTimeout(60000);
        configuration.setMaxErrorRetry(2);
        configuration.setSocketTimeout(60000);
        client = AmazonS3ClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(VEDANTU.equalsIgnoreCase("learnpediaqa") || VEDANTU.equalsIgnoreCase("learnpedialive") ? Regions.US_EAST_1 : Regions.AP_SOUTH_1)
                .withPathStyleAccessEnabled(true).withClientConfiguration(configuration).build();
        iso8601formattter.setTimeZone(VEDANTU.equalsIgnoreCase("learnpediaqa") || VEDANTU.equalsIgnoreCase("learnpedialive") ? TimeZone.getTimeZone("GMT") : TimeZone.getTimeZone("UTC"));
        iso8601formattter2.setTimeZone(TimeZone.getTimeZone("UTC"));
        iso8601formattter3.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {

        return entityType._getStorageId().toLowerCase() + FileUtils.SEPARATOR_HYPHEN
                + fwkId.toLowerCase() + FileUtils.SEPARATOR_HYPHEN + VEDANTU;
    }

    @Override
    public SignUploadFileRes signContentUpload(EntityType entityType, String bucketName, String fileName, String contentType) throws VedantuException {
        return null;
    }

    @Override
    public boolean exists(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override
    public StorageIdentification getIdentification() throws VedantuException {
        return null;
    }

    @Override
    public boolean removeParent(String dirPath) throws VedantuException {
        return false;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws VedantuException {
        return 0;
    }

    @Override
    public FileData getSecureURL(EntityType eType, MediaType mediaType, String fileName) throws VedantuException {
        return null;
    }


    @Override
    public boolean createParent(String dir) throws VedantuException {
        return false;
    }

    @Override
    public boolean store(File localFile, String destDir, String destFileName,
                         Map<String, String> tags) throws FileStoreException {

        try {
            LOGGER.debug("Uploading file : " + localFile.getAbsolutePath());

            store(destDir, destFileName, localFile, tags, localFile.length());
            LOGGER.debug("Uploaded file : " + localFile.getAbsolutePath() + " Result ");

        } catch (Exception exp) {
            throw new FileStoreException("Could not upload file " + localFile.getAbsolutePath()
                    + " to s3Handler at " + destDir, exp);

        }
        return true;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName) throws VedantuException {
        return null;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean copy(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName) throws VedantuException {
        return false;
    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size) throws VedantuException {
        return null;
    }

    @Override
    public String getParentName(EntityType entityType, String fwkId) {
        return null;
    }*/
}
