package com.vedantu.ext.cmds.db.models;

public class Organization extends AbstractDBModel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    public String             adminUserId;
    public String             name;
    public String             thumb;
    public String             id;
    public String             slug;
    public String             mac;
    public String             authToken;
    public String             secretKey;
    public String             key;

    public String             host;

    public Organization() {

        super();
    }

    public Organization(int orgKeyId, String adminUserId, String name, String thumb, String id,
            String slug, String mac, String authToken, String secretKey, String key, String host) {

        super(orgKeyId);
        this.adminUserId = adminUserId;
        this.name = name;
        this.thumb = thumb;
        this.id = id;
        this.slug = slug;
        this.mac = mac;
        this.authToken = authToken;
        this.secretKey = secretKey;
        this.key = key;
        this.host = host;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{adminUserId:").append(adminUserId).append(", name:").append(name)
                .append(", thumb:").append(thumb).append(", id:").append(id).append(", slug:")
                .append(slug).append(", mac:").append(mac).append(", authToken:").append(authToken)
                .append(", secretKey:").append(secretKey).append(", key:").append(key)
                .append(", host:").append(host).append(", _id:").append(_id).append(", orgKeyId:")
                .append(orgKeyId).append(", timeCreated:").append(timeCreated).append("}");
        return builder.toString();
    }

}
