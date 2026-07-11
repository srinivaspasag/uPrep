package com.vedantu.organization.pojos;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class ExternalOrganizationEndpoints {

    private String                              authEndpoint;

    private String                              registerEndpoint;

    private String                              enrollmentEndPoint;

    private String                              testAttemptDataUploadEndpoint;

    private boolean                             allowOfflineLoginOnMobile;

    private List<ExternalOrganizationEndpoints> reqParams;

    public String getAuthEndpoint() {

        return authEndpoint;
    }

    public void setAuthEndpoint(String authEndpoint) {

        this.authEndpoint = StringUtils.defaultString(authEndpoint);
    }

    public String getTestAttemptDataUploadEndpoint() {

        return testAttemptDataUploadEndpoint;
    }

    public void setTestAttemptDataUploadEndpoint(String testAttemptDataUploadEndpoint) {

        this.testAttemptDataUploadEndpoint = StringUtils
                .defaultString(testAttemptDataUploadEndpoint);
    }

    public boolean isAllowOfflineLoginOnMobile() {

        return allowOfflineLoginOnMobile;
    }

    public void setAllowOfflineLoginOnMobile(boolean allowOfflineLoginOnMobile) {

        this.allowOfflineLoginOnMobile = allowOfflineLoginOnMobile;
    }

    public String getRegisterEndpoint() {

        return registerEndpoint;
    }

    public void setRegisterEndpoint(String registerEndpoint) {

        this.registerEndpoint = StringUtils.defaultString(registerEndpoint);
    }

    public String getEnrollmentEndPoint() {

        return enrollmentEndPoint;
    }

    public void setEnrollmentEndPoint(String enrollmentEndPoint) {

        this.enrollmentEndPoint = StringUtils.defaultString(enrollmentEndPoint);
    }

    public List<ExternalOrganizationEndpoints> getReqParams() {

        return reqParams;
    }

    public void setReqParams(List<ExternalOrganizationEndpoints> reqParams) {

        this.reqParams = reqParams;
    }

    public void convertNullToEmptyValues() {

        this.authEndpoint = StringUtils.defaultString(this.authEndpoint);
        this.enrollmentEndPoint = StringUtils.defaultString(this.enrollmentEndPoint);
        this.registerEndpoint = StringUtils.defaultString(this.registerEndpoint);
        this.testAttemptDataUploadEndpoint = StringUtils
                .defaultString(this.testAttemptDataUploadEndpoint);
    }
}
