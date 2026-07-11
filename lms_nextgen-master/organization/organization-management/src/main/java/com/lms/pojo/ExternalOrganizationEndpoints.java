package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class ExternalOrganizationEndpoints {


    private String                              authEndpoint;

    private String                              registerEndpoint;

    private String                              enrollmentEndPoint;

    private String                              testAttemptDataUploadEndpoint;

    private boolean                             allowOfflineLoginOnMobile;

    private List<ExternalOrganizationEndpoints> reqParams;









    public List<ExternalOrganizationEndpoints> getReqParams() {

        return reqParams;
    }

    public void setReqParams(List<ExternalOrganizationEndpoints> reqParams) {

        this.reqParams = reqParams;
    }

    public void convertNullToEmptyValues() {

        this.authEndpoint = this.authEndpoint;
        this.enrollmentEndPoint =this.enrollmentEndPoint;
        this.registerEndpoint = this.registerEndpoint;
        this.testAttemptDataUploadEndpoint = this.testAttemptDataUploadEndpoint;
    }
}
