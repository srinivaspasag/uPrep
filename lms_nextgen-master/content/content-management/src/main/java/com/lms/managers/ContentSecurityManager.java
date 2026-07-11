package com.lms.managers;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.utils.EncryptionUtils;
import com.lms.common.vedantu.enums.EncryptionLevel;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.*;
import com.lms.repository.DocumentsRepo;
import com.lms.repository.LibraryContentLinksRepo;
import com.lms.repository.OrganizationRepo;
import com.lms.repository.VideoRepo;
import com.lms.user.vedantu.user.model.User;
import com.lms.user.vedantu.user.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;

@Component
public class ContentSecurityManager {

    private static final Logger logger = LoggerFactory.getLogger(ContentSecurityManager.class);
    @Autowired
    private LibraryContentLinksRepo libraryContentLinksRepo;
    @Autowired
    private OrganizationRepo organizationRepo;
    @Autowired
    private DocumentsRepo documentsRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private VideoRepo videoRepo;

    public EncryptionLevel getEncLevel(String linkId, String orgId) throws VedantuException {

        Optional<LibraryContentLink> contentLink = libraryContentLinksRepo.findById(linkId);
        if (!contentLink.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }
        return getEncLevel(contentLink.get(), orgId);
    }

    public EncryptionLevel getEncLevel(LibraryContentLink contentLink, String orgId)
            throws VedantuException {

        if (contentLink == null) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }
        Optional<Organization> organizationOptional = organizationRepo.findById(orgId);
        if (!organizationOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "organization not found");
        }
        Organization organization = organizationOptional.get();
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

        Optional<LibraryContentLink> contentLink = libraryContentLinksRepo.findById(linkId);
        if (!contentLink.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ACCESS_DENIED, "invalid content link");
        }
        return getPassphrase(overrridingEncryptionLevel, contentLink.get(), userId, orgId);
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
        VedantuBaseMongoModel mongoModel = null;
        if (contentLink.source.type.equals(EntityType.DOCUMENT)) {
            Optional<Documents> documents = documentsRepo.findById(contentLink.source.id);
            if (documents.isPresent())
                mongoModel = documents.get();
        } else if (contentLink.source.type.equals(EntityType.VIDEO)) {
            Optional<Video> videoOptional = videoRepo.findById(contentLink.source.id);
            if (videoOptional.isPresent())
                mongoModel = videoOptional.get();
        }
        if (mongoModel == null) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                    "content type not supported");
        }
        if (!(mongoModel instanceof AbstractFileModel)) {
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_CONTENT_TYPE,
                    "content type not supported");
        }
        Optional<Organization> organizationOptional = organizationRepo.findById(orgId);

        if (!organizationOptional.isPresent()) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND,
                    "organization not found");
        }
        Organization organization = organizationOptional.get();
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
            organizationRepo.save(organization);

        }
        String passphrase = ((AbstractFileModel) mongoModel).passphrase;
        logger.debug("orig model passPhrase : " + passphrase);
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
                Optional<User> userOptional = null;

                if (StringUtils.isEmpty(userId)) {

                    throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND, "user not found");
                }

                userOptional = userRepo.findById(userId);
                if (!userOptional.isPresent()) {
                    throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND, "user not found");
                }
                User user = userOptional.get();

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

        logger.debug("encLevel:" + contentLink.getEncLevel() + " returning passphrase : "
                + passphrase);
        return passphrase;

    }
}
