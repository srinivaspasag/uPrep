package com.vedantu.organization.pojos;

public class ExternalOrganizationEndPointReqParams {

    public String key;
    public String value;

    public ExternalOrganizationEndPointReqParams(String key, String value) {

        super();
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{key:").append(key).append(", value:").append(value).append("}");
        return builder.toString();
    }

}
