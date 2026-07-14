package com.vedantu.tests.video;

import org.junit.Before;
import org.junit.Test;

import com.vedantu.commons.fs.exception.FileStoreException;
import com.vedantu.commons.fs.handlers.S3Handler;

public class S3HandlerTest {

    @Before
    public void setUp() {

    }

    @Test
    public void createBucket() throws FileStoreException {

        S3Handler handler = new S3Handler();
        handler.createParent("mybucket");

    }
}
