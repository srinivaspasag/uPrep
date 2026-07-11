package com.lms.organization.auth;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.models.Organization;
import com.lms.repository.OrganizationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
@Component
public class AuthHandlerFactory {
    @Autowired
    private OrganizationRepo organizationRepo;


        private static Map<AuthType, Class<? extends AuthHandler>> authHandlerMap = new HashMap<AuthType, Class<? extends AuthHandler>>();
        private static AuthHandlerFactory                          instance       = new AuthHandlerFactory();

    private AuthHandlerFactory() {

        authHandlerMap.put(AuthType.VEDANTU, VedantuAuthHandler.class);
        authHandlerMap.put(AuthType.EXT_AUTH_ORG, ExtAuthHandler.class);
    }

        public static AuthHandlerFactory getInstance() {

        if (instance == null) {
            instance = new AuthHandlerFactory();
        }
        return instance;
    }

        public AuthHandler getAuthHandler(String orgId) throws VedantuException {
            
        Optional<Organization> organization = organizationRepo.findById(orgId.trim());
        if (!organization.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }
        AuthType authType = organization.get().getAuthType() == null ? AuthType.VEDANTU : organization.get().getAuthType();

        try {
            return authHandlerMap.get(authType).getConstructor(Organization.class)
                    .newInstance(organization.get());
        } catch (Exception e) {
            return new VedantuAuthHandler(organization.get());
        }

    }

        public AuthHandler getAuthHandler(Organization organization) throws VedantuException {

        AuthType authType = organization.authType == null ? AuthType.VEDANTU
                : organization.authType;

        try {
            return authHandlerMap.get(authType).getConstructor(Organization.class)
                    .newInstance(organization);
        } catch (Exception e) {
            return new VedantuAuthHandler(organization);
        }
    }

}
