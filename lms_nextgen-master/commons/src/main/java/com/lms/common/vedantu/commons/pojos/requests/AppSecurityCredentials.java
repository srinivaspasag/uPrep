package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AppSecurityCredentials {
    private String appId;
    private  String authToken;
    private String secretKey;

    @Override
    public int hashCode() {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((appId == null) ? 0 : appId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AppSecurityCredentials other = (AppSecurityCredentials) obj;
        if (appId == null) {
            if (other.appId != null)
                return false;
        } else if (!appId.equals(other.appId))
            return false;
        return true;
    }
}
