package com.vedantu.organizations.auth;

import java.util.HashMap;
import java.util.Map;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;

public class AuthHandlerFactory {

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

        Organization organization = OrganizationDAO.INSTANCE.getById(orgId);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "no org found with id: " + orgId);
        }
        AuthType authType = organization.authType == null ? AuthType.VEDANTU
                : organization.authType;

        try {
            return authHandlerMap.get(authType).getConstructor(Organization.class)
                    .newInstance(organization);
        } catch (Exception e) {
            return new VedantuAuthHandler(organization);
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
