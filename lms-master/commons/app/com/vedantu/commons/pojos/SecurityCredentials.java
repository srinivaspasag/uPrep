package com.vedantu.commons.pojos;

/**
 * This wil store encoded public key and encoded private key pair In case of RSA public key is
 * encoded with X509EncodedKeySpec private Key is encoded with PKCS #8 encoding
 * 
 * @author vikram
 * 
 */
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