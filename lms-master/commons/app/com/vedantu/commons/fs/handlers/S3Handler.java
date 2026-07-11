package com.vedantu.commons.fs.handlers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.restlet.data.Range;
import org.restlet.engine.http.header.RangeReader;
import org.restlet.representation.FileRepresentation;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.mvc.Http;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.HttpMethod;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.CreateBucketRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.transfer.Transfer;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.util.Base16Lower;
import com.vedantu.commons.constants.HttpConstants;
import com.vedantu.commons.entity.factory.EntityStorageFactory;
import com.vedantu.commons.entity.storage.IEntityFileStorage;
import com.vedantu.commons.entity.storage.MediaType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.StorageIdentification;
import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.managers.AmazonS3Manager;
import com.vedantu.commons.fs.managers.ObjectStorageManager;
import com.vedantu.commons.fs.objectstorage.Container;
import com.vedantu.commons.fs.responses.SignUploadFileRes;
import com.vedantu.commons.http.HTTPHeaderFormatter;
import com.vedantu.commons.pojos.FileData;
import com.vedantu.commons.utils.ContentTypeMapper;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.commons.utils.FileUtils;

/**
 * {@link http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html}
 *
 * @author vikram
 *
 */
public class S3Handler implements IFileSystemHandler {

    public static final String   VEDANTU                          = Play.application()
                                                                          .configuration()
                                                                          .getString(
                                                                                  "amazon.s3.bucket.identifier");
    public static final String  frameworkId = Play.application().configuration().getString("fwkId");

    public static final int      MAXIMUM_EXPIRATION_TIME_OFFSET   = 3600 * 1000;

    private final static ALogger LOGGER                           = Logger.of(S3Handler.class);

    private AWSCredentials       awsCredentials                   = null;
    // Time format iso-8601-format
    SimpleDateFormat             iso8601formattter                = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat             iso8601formattter2                = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
    SimpleDateFormat             iso8601formattter3                = new SimpleDateFormat("yyyyMMdd");

    public static final String   HMAC_SHA1_ALGORITHM              = "HmacSHA1";
    public static final String   HMAC_SHA256_ALGORITHM            = "HmacSHA256";
    // http://docs.aws.amazon.com/AmazonS3/latest/API/ErrorResponses.html
//    private static final String  AMAZON_S3_ERROR_CODE_NO_SUCH_KEY = "NoSuchKey";
    private AmazonS3             client                           = null;

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
    public FileData get(String sourceDir, String srcFileName) throws FileStoreException {

        try {

            S3Object amazonS3StoredObject = client.getObject(sourceDir, srcFileName);

            Map<String, String> tags = new HashMap<String, String>();
            tags.putAll(amazonS3StoredObject.getObjectMetadata().getUserMetadata());
            tags.put(play.mvc.Http.HeaderNames.ETAG, amazonS3StoredObject.getObjectMetadata()
                    .getETag());
            if (amazonS3StoredObject.getObjectMetadata() != null
                    && amazonS3StoredObject.getObjectMetadata().getExpirationTime() != null) {
                tags.put(play.mvc.Http.HeaderNames.EXPIRES, HTTPHeaderFormatter.formatter
                        .format(amazonS3StoredObject.getObjectMetadata().getExpirationTime()));
            }
            tags.put(play.mvc.Http.HeaderNames.LAST_MODIFIED, HTTPHeaderFormatter.formatter
                    .format(amazonS3StoredObject.getObjectMetadata().getLastModified()));
            LOGGER.debug(" User provided tags :"
                    + amazonS3StoredObject.getObjectMetadata().getUserMetadata());

            LOGGER.debug("Retrieved file : " + srcFileName + " tags" + tags
                    + " file size available " + amazonS3StoredObject.getObjectContent().available());
            FileData data = new FileData(tags, amazonS3StoredObject.getObjectContent());
            data.setContentLength(amazonS3StoredObject.getObjectMetadata().getContentLength());
            // data.setTotalContentLength(amazonS3StoredObject.getObjectMetadata().getContentLength());
            data.setFileSize(amazonS3StoredObject.getObjectMetadata().getContentLength());
            return data;
        } catch (AmazonServiceException exception) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;

            LOGGER.error(retrieveError, exception);
            throw new FileStoreException(retrieveError);
        } catch (AmazonClientException exception) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;
            LOGGER.error(retrieveError, exception);
            throw new FileStoreException(retrieveError);
        } catch (IOException e) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;
            LOGGER.error(retrieveError, e);
            throw new FileStoreException(retrieveError);
        }

    }

    @Override
    public boolean createParent(String dir) throws FileStoreException {

        LOGGER.debug("checking if bucket:" + dir + " exist");
        if (client.doesBucketExistV2(dir)) {
            return true;
        }
        synchronized (this) {
            try {
                if (client.doesBucketExistV2(dir)) {
                    return true;
                }
                CreateBucketRequest bucketCreationRequest = new CreateBucketRequest(dir);
                bucketCreationRequest.setCannedAcl(CannedAccessControlList.Private);

                client.createBucket(bucketCreationRequest);
                client.setBucketCrossOriginConfiguration(dir,
                        (AmazonS3Manager.get().getCORSConfiguration()));

            } catch (AmazonServiceException exception) {
                LOGGER.error(" Failed to create bucket", exception);
                throw new FileStoreException("Could not create container " + dir
                        + "on S3 amazon aws at " + dir, exception);
            } catch (AmazonClientException exception) {
                LOGGER.error(" Failed to create bucket", exception);
                throw new FileStoreException("Could not create container " + dir
                        + "on S3 amazon aws at " + dir, exception);
            }
        }
        return true;
    }

    @Override
    public boolean delete(String sourceDir, String srcFileName) throws FileStoreException {

        try {

            client.deleteObject(sourceDir, srcFileName);

        } catch (AmazonServiceException exception) {
            LOGGER.error("Could not delete file from S3 amazon aws at " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName, exception);
            throw new FileStoreException("Could not get file from S3 amazon aws at " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName);
        } catch (AmazonClientException exception) {
            LOGGER.error("Could not delete file from ObjectStorage at " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName, exception);
            throw new FileStoreException("Could not get file from S3 amazon aws at " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName);
        }

        return true;
    }

    @Override
    public boolean copy(String srcDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        try {
            createParent(destDir);
            LOGGER.debug("Moving file from " + srcDir + " / " + srcFileName + " to  " + destDir
                    + " / " + destFileName);
            client.copyObject(srcDir, srcFileName, destDir, destFileName);

        } catch (AmazonServiceException exception) {
            final String copyError = "Could not copy file from S3 amazon aws from " + srcDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName + "  to " + destDir
                    + FileUtils.SEPARATOR_FWDSLASH + destFileName;

            LOGGER.error(copyError, exception);
            throw new FileStoreException(copyError);
        } catch (AmazonClientException exception) {
            final String copyError = "Could not copy file from S3 amazon aws from " + srcDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName + "  to " + destDir
                    + FileUtils.SEPARATOR_FWDSLASH + destFileName;
            LOGGER.error(copyError, exception);
            throw new FileStoreException(copyError);
        }

        return true;
    }

    @Override
    public boolean move(String sourceDir, String destDir, String srcFileName, String destFileName)
            throws FileStoreException {

        try {
            createParent(destDir);

            client.copyObject(sourceDir, srcFileName, destDir, destFileName);
            client.deleteObject(sourceDir, srcFileName);
        } catch (AmazonServiceException exception) {
            final String copyError = "Could not move file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName + "  to " + destDir
                    + FileUtils.SEPARATOR_FWDSLASH + destFileName;

            LOGGER.error(copyError, exception);
            throw new FileStoreException(copyError);
        } catch (AmazonClientException exception) {
            final String copyError = "Could not mpve file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName + "  to " + destDir
                    + FileUtils.SEPARATOR_FWDSLASH + destFileName;
            LOGGER.error(copyError, exception);
            throw new FileStoreException(copyError);
        }
        return true;
    }

    public static void main(String args[]) {

        try {

            S3Handler handler = new S3Handler();
            System.out.println(" storing to object store ");
            // handler.createParent("newtest");
            System.out.println(" created document ");

            Map<String, String> tags = new HashMap<String, String>();
            tags.put("id", "myphoto");
            tags.put("date", "jan25");

            tags.put("Content-type", "image/jpeg");
            handler.store(new File("/home/vikram/Documents/1.jpg"), "test_documents",
                    "testfile1.jpeg", tags);
            FileData data = handler.get("test_documents", "testfile1.jpeg");
            System.out.println(data.getFileMetaInfo());
            // handler.delete("documents", "abcd.jpeg") ;

            Container container = ObjectStorageManager.get().getContainer("test_documents");
            System.out.println(container.listObjectFiles());
            // handler.delete("documents", "abcd.jpeg");
            FileData dataw = handler.get("test_documents", "testfile1.jpeg");
            System.out.println(" TagInfo :" + dataw.getFileMetaInfo());

            // handler.move("test_documents",
            // "test_documents2","testfile1.jpeg", "test5.jpeg");

            handler.copy("test_documents", "test_documents2", "testfile1.jpeg", "test9.jpeg");
        } catch (Exception exp) {
            exp.printStackTrace();
        }

    }

    @Override
    public FileData get(String sourceDir, String srcFileName, long index, long size)
            throws FileStoreException {

        try {

            GetObjectRequest getObjectRequest = new GetObjectRequest(sourceDir, srcFileName);
            if (size != -1) {
                getObjectRequest.setRange(index, index + (size - 1));

            } else if (index != 0) {
                ObjectMetadata contentData = client.getObjectMetadata(new GetObjectMetadataRequest(
                        sourceDir, srcFileName));
                getObjectRequest.setRange(index, contentData.getContentLength() - 1);
            }
            LOGGER.debug("S3 requested Range: " + getObjectRequest.getRange());
            LOGGER.debug("S3 Retrieving file : " + getObjectRequest.getBucketName() + " / "
                    + getObjectRequest.getKey());

            S3Object amazonS3StoredObject = client.getObject(getObjectRequest);

            LOGGER.debug("S3 metadata: " + amazonS3StoredObject.getObjectMetadata());
            LOGGER.debug("S3 user metadata: "
                    + amazonS3StoredObject.getObjectMetadata().getUserMetadata());

            Map<String, String> tags = new HashMap<String, String>();
            tags.putAll(amazonS3StoredObject.getObjectMetadata().getUserMetadata());
            tags.put(play.mvc.Http.HeaderNames.ETAG, amazonS3StoredObject.getObjectMetadata()
                    .getETag());

            LOGGER.debug("S3 metadata expiration time: "
                    + amazonS3StoredObject.getObjectMetadata().getExpirationTime());
            LOGGER.debug("S3 metadata lastmodified time: "
                    + amazonS3StoredObject.getObjectMetadata().getLastModified());

            tags.put(play.mvc.Http.HeaderNames.LAST_MODIFIED, HTTPHeaderFormatter.formatter
                    .format(amazonS3StoredObject.getObjectMetadata().getLastModified()));
            LOGGER.debug(" User provided tags :"
                    + amazonS3StoredObject.getObjectMetadata().getUserMetadata());

            LOGGER.debug("Retrieved file : " + srcFileName + " tags" + tags
                    + " file stream size available "
                    + amazonS3StoredObject.getObjectContent().available());

            FileData data = new FileData(tags, amazonS3StoredObject.getObjectContent());
            String contentRange = (String) amazonS3StoredObject.getObjectMetadata()
                    .getRawMetadata().get(HttpConstants.HTTP_RESPONSE_HEADER_CONTENT_RANGE);
            LOGGER.debug(" contentRange : " + contentRange);

            if (StringUtils.isNotEmpty(contentRange)) {
                FileRepresentation representation = new FileRepresentation("",
                        org.restlet.data.MediaType.VIDEO_MP4);
                RangeReader.update(contentRange, representation);

                Range range = representation.getRange(); // we will read only
                                                         // first
                                                         // range if its a list

                LOGGER.debug(" Parsed data index " + range.getIndex() + " size " + range.getSize()
                        + " " + representation.getSize());

                data.setFileSize(representation.getSize());
                // data.setTotalContentLength(representation.getSize());
            } else {
                data.setFileSize(amazonS3StoredObject.getObjectMetadata().getContentLength());
                // data.setTotalContentLength(amazonS3StoredObject.getObjectMetadata()
                // .getContentLength());

            }

            data.setContentLength(amazonS3StoredObject.getObjectMetadata().getContentLength());
            return data;
        } catch (AmazonServiceException exception) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;

            LOGGER.error(retrieveError, exception);
            throw new FileStoreException(retrieveError);
        } catch (AmazonClientException exception) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;
            LOGGER.error(retrieveError, exception);
            throw new FileStoreException(retrieveError);
        } catch (IOException exception) {
            final String retrieveError = "Could not get file from S3 amazon aws from " + sourceDir
                    + FileUtils.SEPARATOR_FWDSLASH + srcFileName;
            LOGGER.error(retrieveError, exception);
            throw new FileStoreException(retrieveError);
        }

    }

    public boolean store(String bucketName, String key, File file, Map<String, String> tags,
            long totalFileSize) throws FileStoreException, FileNotFoundException {

        createParent(bucketName);

        ObjectMetadata metadata = new ObjectMetadata();
        PutObjectRequest request = file.isDirectory() ? new PutObjectRequest(bucketName, key, file)
                : new PutObjectRequest(bucketName, key, new FileInputStream(file), metadata);
        request.setCannedAcl(CannedAccessControlList.Private);
        metadata.setContentType(ContentTypeMapper.get().getContentType(key));

        metadata.setContentLength(totalFileSize);
        if (tags != null) {
            metadata.setUserMetadata(tags);
        }
        TransferManager manager = TransferManagerBuilder.standard().withS3Client(client).build();
        Transfer myupload = null;
        if (file.isDirectory()) {
            LOGGER.info("uploading directory " + request.getFile() + " to S3");
            myupload = manager.uploadDirectory(request.getBucketName(), request.getKey(),
                    request.getFile(), true);
        } else {
            myupload = manager.upload(request);
        }

        while (myupload.isDone() == false) {
            LOGGER.info("Uploading file " + key + " % completed "
                    + myupload.getProgress().getBytesTransferred() + " of "
                    + myupload.getProgress().getTotalBytesToTransfer());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                LOGGER.debug("Interuppted thread ");
            }
        }
        return myupload.isDone();
        // return client.putObject(bucketName, key, stream, metadata) != null;
    }

    /**
     * @throws FileStoreException
     *             This is with presigned URL
     * @throws
     */
    @Override
    public SignUploadFileRes signContentUpload(EntityType type, String bucketName, String fileName,
            String contentType) throws FileStoreException {
        createParent(bucketName);
        try {
            if(VEDANTU.equalsIgnoreCase("learnpediaqa") || VEDANTU.equalsIgnoreCase("learnpedialive")){
                // OLD Buckets
                return signContentUploadV2(type, bucketName, fileName, contentType);
            }else{
                // New Buckets
                return signContentUploadV4(type, bucketName, fileName, contentType);
            }
        } catch (Exception exception) {
            throw new FileStoreException(exception.getMessage());
        }
    }

    public SignUploadFileRes signContentUploadV2(EntityType type, String bucketName, String fileName,
            String contentType) throws FileStoreException, UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException {
        SignUploadFileRes signedURLRes = new SignUploadFileRes();
        long currentMilliseconds = System.currentTimeMillis();
        String expirationTime = iso8601formattter.format(new Date(currentMilliseconds + MAXIMUM_EXPIRATION_TIME_OFFSET));
        String policyDocument = "{\"expiration\": \"" + expirationTime + "\","
                + "\"conditions\": [" + "{\"bucket\": \"" + bucketName + "\"},"
                + "[\"starts-with\", \"$key\", \"\"]," + "{\"acl\": \"" + "private" + "\"},"
                + "[\"starts-with\", \"$Content-Type\", \"\"]"+
                "]" + "}";
        String policy = EncryptionUtils.getAsciiByte64Encoding(policyDocument);
        signedURLRes.url = "https://s3.amazonaws.com/" + bucketName;
        signedURLRes.requestParams.put("signature", sign(policy));
        signedURLRes.requestParams.put("policy", policy);
        signedURLRes.requestParams.put("AWSAccessKeyId", awsCredentials.getAWSAccessKeyId());
        signedURLRes.requestParams.put("acl", "private");
        signedURLRes.requestParams.put("key", fileName);
        signedURLRes.requestParams.put("Content-Type", contentType);
        signedURLRes.verificationRequired = false;
        LOGGER.debug(" Calculated ContentType " + contentType);
        signedURLRes.contentType = contentType;
        return signedURLRes;
    }

    public SignUploadFileRes signContentUploadV4(EntityType type, String bucketName, String fileName,
            String contentType) throws Exception {
        SignUploadFileRes signedURLRes = new SignUploadFileRes();
        long currentMilliseconds = System.currentTimeMillis();
        String expirationTime = iso8601formattter.format(new Date(currentMilliseconds + MAXIMUM_EXPIRATION_TIME_OFFSET));
        Date date =  new Date(currentMilliseconds);
        String dateKey = iso8601formattter3.format(date);
        String amzDate = iso8601formattter2.format(date);
        String credentials = awsCredentials.getAWSAccessKeyId()+"/"+dateKey+"/"+Regions.AP_SOUTH_1.getName()+"/s3/aws4_request";
        String policyDocument = "{\"expiration\": \"" + expirationTime + "\","
                + "\"conditions\": [" + "{\"bucket\": \"" + bucketName + "\"},"
                + "[\"starts-with\", \"$key\", \""+fileName+"\"]," + "{\"acl\": \"" + "private" + "\"},"
                + "[\"starts-with\", \"$Content-Type\", \""+contentType+"\"],"
                + "{\"x-amz-algorithm\": \"" + "AWS4-HMAC-SHA256" + "\"},"
                + "{\"x-amz-credential\": \"" + credentials + "\"},"
                + "{\"x-amz-date\": \"" + amzDate + "\"}"+
                "]" + "}";
        String policy = EncryptionUtils.getAsciiByte64Encoding(policyDocument);
        signedURLRes.url = "https://s3."+Regions.AP_SOUTH_1.getName()+".amazonaws.com/" + bucketName;
        byte[] signingKey = getSignatureKey(awsCredentials.getAWSSecretKey(), dateKey, Regions.AP_SOUTH_1.getName(), "s3");
        signedURLRes.requestParams.put("x-amz-signature", signHex(policy,signingKey));
        signedURLRes.requestParams.put("policy", policy);
        signedURLRes.requestParams.put("x-amz-algorithm", "AWS4-HMAC-SHA256");
        signedURLRes.requestParams.put("x-amz-credential", credentials);
        signedURLRes.requestParams.put("x-amz-date", amzDate);
//        signedURLRes.requestParams.put("AWSAccessKeyId", awsCredentials.getAWSAccessKeyId());
        signedURLRes.requestParams.put("acl", "private");
        signedURLRes.requestParams.put("key", fileName);
        signedURLRes.requestParams.put("Content-Type", contentType);
        signedURLRes.verificationRequired = false;
        LOGGER.debug(" Calculated ContentType " + contentType);
        signedURLRes.contentType = contentType;
        return signedURLRes;
    }

    private byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes("UTF-8");
        byte[] kDate = HmacSHA256(dateStamp, kSecret);
        byte[] kRegion = HmacSHA256(regionName, kDate);
        byte[] kService = HmacSHA256(serviceName, kRegion);
        byte[] kSigning = HmacSHA256("aws4_request", kService);
        return kSigning;
    }

    static byte[] HmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_SHA256_ALGORITHM));
        return mac.doFinal(data.getBytes("UTF-8"));
    }

    private String sign(String data) throws NoSuchAlgorithmException, UnsupportedEncodingException,
            InvalidKeyException {

        Mac hmac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
        hmac.init(new SecretKeySpec(awsCredentials.getAWSSecretKey()
                .getBytes(EncryptionUtils.UTF_8), HMAC_SHA1_ALGORITHM));

        return new String(Base64.encodeBase64(hmac.doFinal(data.getBytes(EncryptionUtils.UTF_8))),
                EncryptionUtils.ASCII);

    }

    private String signHex(String policy, byte[] signingKey) throws Exception {
        byte[] kDate = HmacSHA256(policy, signingKey);
        return Base16Lower.encodeAsString(kDate);
    }

    @Override
    public boolean exists(String bucketName, String key) throws FileStoreException {

        if (!client.doesBucketExistV2(bucketName)) {
            LOGGER.debug("Bucket not found :" + bucketName);
            return false;
        }

        GetObjectMetadataRequest request = new GetObjectMetadataRequest(bucketName, key);
        ObjectMetadata response = null;
        try {
            response = client.getObjectMetadata(request);
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != Http.Status.NOT_FOUND) {
                return false;
            }
        }
        if (response == null) {
            return false;
        }

        return true;
    }

    /**
     * This is with presigned URL
     */
    // @Override
    // public URL signContentUpload(String bucketName, String fileName, String
    // contentType) {
    //
    // long expiryTime = 3600 * 1000;
    // AmazonS3Client client = new AmazonS3Client(awsCredentials);
    // String generatedFileName = fileName;
    // GeneratePresignedUrlRequest request = new
    // GeneratePresignedUrlRequest(bucketName,
    // generatedFileName);
    // request.setMethod(HttpMethod.POST);
    // request.setExpiration(new Date(System.currentTimeMillis() + expiryTime));
    // request.setRequestCredentials(awsCredentials);
    // request.setContentType(contentType);
    // LOGGER.debug(" Calculated ContentType " + contentType);
    //
    // URL signedURL = client.generatePresignedUrl(request);
    // return signedURL;
    //
    // // EntityType type = EntityType.VIDEO;
    // // IEntityFileStorage storage = EntityStorageFactory.INSTANCE.get(type);
    // // String amazonURL = "http://s3.amazonaws.com/";
    // // long expiryTime = 300;
    // // String requestType = AllowedMethods.PUT.name();
    // // String mimeTye = "text/html";
    // // long expires = System.currentTimeMillis() + expiryTime;
    // // String amzHeaders = "x-amz-acl:private-read";
    // // String stringToSign = ""+
    // requestType+"\n\n"+mimeTye+"\n"+expires+"\n"+amzHeaders+"\n"+
    // // storage.
    // //
    //
    // }

    @Override
    public StorageIdentification getIdentification() throws FileStoreException {

        return StorageIdentification.S3;
    }

    @Override
    public boolean removeParent(String dirPath) throws FileStoreException {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long size(String sourceDir, String srcFileName) throws FileStoreException {

        if (!client.doesBucketExistV2(sourceDir)) {
            LOGGER.debug("Bucket not found :" + sourceDir);
            return -1;
        }

        GetObjectMetadataRequest request = new GetObjectMetadataRequest(sourceDir, srcFileName);
        ObjectMetadata response = null;
        try {
            response = client.getObjectMetadata(request);
            return response.getContentLength();
        } catch (AmazonS3Exception e) {
            if (e.getStatusCode() != Http.Status.NOT_FOUND) {
                return -1;
            }
        }
        if (response == null) {
            return -1;
        }

        return -1;
    }

    @Override
    public FileData getSecureURL(EntityType eType, MediaType mediaType, String fileName) {

        IEntityFileStorage storage = EntityStorageFactory.INSTANCE.get(eType);

        if (!client.doesBucketExistV2(storage.getStorageId())) {
            LOGGER.debug("Bucket not found :" + storage.getStorageId());
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
            metadataReq = new GetObjectMetadataRequest(storage.getStorageId(), fileName);
            metadata = client.getObjectMetadata(metadataReq);
        } catch (Exception exp) {
            LOGGER.debug("File not found :" + storage.getStorageId() + "/" + fileName);
            return null;
        }
        data.setContentLength(metadata.getContentLength());
        LOGGER.debug("found content length content size " + metadata.getContentLength());
        GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
                storage.getStorageId(), fileName);
        generatePresignedUrlRequest.setMethod(HttpMethod.GET);
        generatePresignedUrlRequest.setExpiration(expiration);

        URL url = client.generatePresignedUrl(generatePresignedUrlRequest);

        LOGGER.debug("Pre-Signed URL = " + url.toString());

        data.setSecuredURL(url.toString());
        return data;

    }
}
