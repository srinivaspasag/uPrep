package com.vedantu.commons.fs.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Play;

import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.CORSRule.AllowedMethods;
import com.vedantu.commons.constants.Configurations;

public class AmazonS3Manager {

    private static AmazonS3Manager instance        = null;
    public String                  accessKeyId     = null;
    public String                  secretAccessKey = null;

    private AmazonS3Manager() {

        // accessKeyId = "__AWS_ACCESS_KEY_REDACTED__";
        // secretAccessKey = "1o6Jfn7Fc8ig7c8qTX7CkcxdaQVEzrYExZ5RTwIk";
        accessKeyId = Play.application().configuration()
                .getString(Configurations.AMAZON_S3_ACCESS_KEY);
        secretAccessKey = Play.application().configuration()
                .getString(Configurations.AMAZON_S3_SECRET_KEY);
    }

    public static AmazonS3Manager get() {

        if (instance == null) {
            synchronized (AmazonS3Manager.class) {
                if (instance == null) {
                    instance = new AmazonS3Manager();
                }

            }
        }
        return instance;
    }

    public String getAccessKey() {

        return accessKeyId;
    }

    public String getSecretKey() {

        return secretAccessKey;
    }

    /**
     * Retrived cors configuration on bucket currently its common for all
     * 
     * @return
     */

    public BucketCrossOriginConfiguration getCORSConfiguration() {

        CORSRule rule = new CORSRule();
        rule.setAllowedMethods(AllowedMethods.POST, AllowedMethods.GET, AllowedMethods.PUT,
                AllowedMethods.HEAD);
        rule.setMaxAgeSeconds(3600);
        BucketCrossOriginConfiguration corsConfiguration = new BucketCrossOriginConfiguration();
        corsConfiguration.setRules(Arrays.asList(rule));
        List<String> allowedOrigins = new ArrayList<String>();

        List<Object> domains = Play.application().configuration()
                .getList(Configurations.AMAZON_S3_BUCKET_ALLOWEDORIGINS);
        if (CollectionUtils.isNotEmpty(domains)) {
            for (Object domain : domains) {
                Logger.debug("Allowed origin for s3 :" + domain.toString());
                allowedOrigins.add(domain.toString());
            }

            rule.setAllowedOrigins(allowedOrigins);
        }
        rule.setAllowedHeaders("*");

        return corsConfiguration;
    }
    //
    // public ObjectFile getObjectFile(String containerName, String fileName) throws IOException {
    //
    // return new ObjectFile(fileName, containerName, baseUrl, username, password, true);
    // }
    //
    // public Container getContainer(String containerName) throws IOException {
    //
    // return new Container(containerName, baseUrl, username, password, true);
    //
    // }
    //
    // public ObjectFileEx getObjectFileExtended(String containerName, String fileName)
    // throws IOException {
    //
    // return new ObjectFileEx(fileName, containerName, baseUrl, username, password, true);
    // }

}
