package com.lms.common.vedantu.commons.pojos.requests;

public class SecurityCredentials {
    public SecurityCredentials() {

        super();
    }

    public SecurityCredentials(byte[] privateKey, byte[] publicKey) {

        super();
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    private byte[] privateKey;
    private byte[] publicKey;

    public byte[] getPrivateKey() {

        return privateKey;
    }

    public void setPrivateKey(byte[] privateKey) {

        this.privateKey = privateKey;
    }

    public byte[] getPublicKey() {

        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {

        this.publicKey = publicKey;
    }

}

