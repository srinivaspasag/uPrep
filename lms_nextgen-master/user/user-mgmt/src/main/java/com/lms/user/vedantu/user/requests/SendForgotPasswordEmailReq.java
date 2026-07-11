package com.lms.user.vedantu.user.requests;


import javax.validation.constraints.NotBlank;

public class SendForgotPasswordEmailReq extends AbstractAppCheckReq {

        @NotBlank(message = "User name is required")
        private String username;
        private String orgId;

        public String getUsername() {

            return username;
        }

        public void setUsername(String username) {

            this.username = username;
        }


        public String getOrgId() {

            return orgId;
        }


        public void setOrgId(String orgId) {

            this.orgId = orgId;
        }



}
