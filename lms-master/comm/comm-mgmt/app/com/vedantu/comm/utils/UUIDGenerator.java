package com.vedantu.comm.utils;

import java.util.UUID;

public class UUIDGenerator implements IDGenerator {

    @Override
    public String getID() {

        return UUID.randomUUID().toString();
    }

}
