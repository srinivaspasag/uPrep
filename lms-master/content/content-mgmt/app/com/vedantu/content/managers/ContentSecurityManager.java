package com.vedantu.content.managers;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EncryptionLevel;
import com.vedantu.commons.utils.EncryptionUtils;
import com.vedantu.content.daos.LibraryContentLinksDAO;
import com.vedantu.content.models.AbstractFileModel;
import com.vedantu.content.models.LibraryContentLink;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public class ContentSecurityManager {

    private static final ALogger LOGGER = Logger.of(ContentSecurityManager.class);

    public EncryptionLevel getEncLevel(String linkId, String orgId) throws VedantuException {

        LibraryContentLink contentLink = LibraryContentLinksDAO.INSTANCE.getById(linkId);
        return getEncLevel(contentLink, orgId);
    }

    public EncryptionLevel getEncLevel(LibraryContentLink contentLink, String orgId)
            throws VedantuException {

        if (contentLink == null) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }

        Organization organization = OrganizationDAO.INSTANCE.getById(orgId);
        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "organization not found");
        }

        EncryptionLevel calculatedEncLevel = (contentLink.getEncLevel() == null || contentLink
                .getEncLevel() == EncryptionLevel.NA) ? organization.encLevel : contentLink
                .getEncLevel();
        return calculatedEncLevel;
    }

    public String getPassphrase(String linkId, String userId, String orgId) throws VedantuException {

        return getPassphrase(null, linkId, userId, orgId);
    }

    public String getPassphrase(EncryptionLevel overrridingEncryptionLevel, String linkId,
            String userId, String orgId) throws VedantuException {

        LibraryContentLink contentLink = LibraryContentLinksDAO.INSTANCE.getById(linkId);
        return getPassphrase(overrridingEncryptionLevel, contentLink, userId, orgId);
    }

    public String getPassphrase(LibraryContentLink contentLink, String userId, String orgId)
            throws VedantuException {

        return getPassphrase(null, contentLink, userId, orgId);
    }

    public EncryptionLevel getEffectivEncLevel(EncryptionLevel overrridingEncryptionLevel,
            String linkId, String orgId) throws VedantuException {

        EncryptionLevel calculatedEncLevel = getEncLevel(linkId, orgId);
        if (overrridingEncryptionLevel != null) {
            calculatedEncLevel = overrridingEncryptionLevel;
        }
        return calculatedEncLevel;
    }

    public String getPassphrase(EncryptionLevel overrridingEncryptionLevel,
            LibraryContentLink contentLink, String userId, String orgId) throws VedantuException {

        if (contentLink == null) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }

        VedantuBasicDAO<?, ?> dao = EntityTypeDAOFactory.INSTANCE.get(contentLink.source.type);
        if (dao == null) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                    "content type not supported");
        }
        VedantuBaseMongoModel mongoModel = dao.getById(contentLink.source.id);
        if (!(mongoModel instanceof AbstractFileModel)) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                    "content type not supported");
        }
        Organization organization = OrganizationDAO.INSTANCE.getById(orgId);

        if (organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "organization not found");
        }

        boolean organizationCredentialsUpdated = false;
        if (organization.credentials == null) {
            organization.credentials = EncryptionUtils.generateKeys();
            organizationCredentialsUpdated = true;
        }
        if (organization.encLevel == null) {
            organization.encLevel = EncryptionLevel.NA;

            organizationCredentialsUpdated = true;
        }
        if (organizationCredentialsUpdated) {
            OrganizationDAO.INSTANCE.save(organization);

        }
        String passphrase = ((AbstractFileModel) mongoModel).passphrase;
        Logger.debug("orig model passPhrase : " + passphrase);
        if (StringUtils.isEmpty(passphrase)) {
            return null;
        }

        EncryptionLevel calculatedEncLevel = this.getEffectivEncLevel(overrridingEncryptionLevel,
                contentLink._getStringId(), orgId);

        switch (calculatedEncLevel) {
        case NA:
            break;
        case P:
            break;
        case P_O: {
            if (organization.credentials == null) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED,
                        "organization credential keys doesn't exist");
            }
            passphrase = EncryptionUtils.encryptWithPrivateKey(passphrase,
                    organization.credentials.getPrivateKey());
            break;
        }
        case P_O_U: {
            User user = null;

            if (StringUtils.isEmpty(userId)) {

                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND, "user not found");
            }

            user = UserDAO.INSTANCE.getById(userId);
            if (user == null) {
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND, "user not found");
            }

            if (user.credentials == null || organization.credentials == null) {
                throw new VedantuException(VedantuErrorCode.ACCESS_DENIED,
                        "user credential keys doesn't exist");
            }
            String encryptedKey = EncryptionUtils.encryptWithPrivateKey(passphrase,
                    organization.credentials.getPrivateKey());
            String encryptedWithUserPublicKey = EncryptionUtils.encryptWithPublicKey(encryptedKey,
                    user.credentials.getPublicKey());

            passphrase = encryptedWithUserPublicKey;
            break;
        }

        }

        LOGGER.debug("encLevel:" + contentLink.getEncLevel() + " returning passphrase : "
                + passphrase);
        return passphrase;

    }
}
