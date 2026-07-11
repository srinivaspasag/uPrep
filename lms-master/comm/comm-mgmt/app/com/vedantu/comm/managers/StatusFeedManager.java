package com.vedantu.comm.managers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.comm.daos.EntityShareDAO;
import com.vedantu.comm.enums.NotificationReason;
import com.vedantu.comm.enums.ShareType;
import com.vedantu.comm.event.details.ShareEntityDetails;
import com.vedantu.comm.managers.news.NewsFeedSecurityVaildator;
import com.vedantu.comm.managers.news.UserSecuritySet;
import com.vedantu.comm.models.mongo.EntityShare;
import com.vedantu.comm.pojos.news.NewsFeedInfo;
import com.vedantu.comm.requests.AddStatusFeedReq;
import com.vedantu.comm.requests.DeleteStatusFeedReq;
import com.vedantu.comm.requests.GetStatusFeedReq;
import com.vedantu.comm.requests.UploadStatusFileReq;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.constants.FileSystemConstants;
import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.entity.storage.FileCategory;
import com.vedantu.commons.entity.storage.ImageSize;
import com.vedantu.commons.entity.storage.StatusFeedEntityFileStorage;
import com.vedantu.commons.entity.storage.StorageResult;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.commons.fs.exception.FileHandlerFactory;
import com.vedantu.commons.fs.exception.FileHandlerFactory.HandlerType;
import com.vedantu.commons.fs.handlers.FileSystemFactory;
import com.vedantu.commons.fs.handlers.LocalFileSystemHandler;
import com.vedantu.commons.fs.managers.DownloadImageManager;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.FileUtils;
import com.vedantu.commons.utils.ImageDisplayURLUtil;
import com.vedantu.commons.utils.ImageFilter;
import com.vedantu.content.daos.StatusFeedDAO;
import com.vedantu.content.managers.AbstractContentManager;
import com.vedantu.content.models.StatusFeed;
import com.vedantu.content.pojos.Source;
import com.vedantu.content.pojos.SrcType.LinkType;
import com.vedantu.content.pojos.responses.StatusFeedInfo;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.OrgMemberBasicInfo;
import com.vedantu.pojos.response.DeleteStatusFeedRes;
import com.vedantu.pojos.response.GetStatusFeedRes;
import com.vedantu.pojos.response.newsfeed.AddStatusFeedRes;
import com.vedantu.pojos.response.newsfeed.UploadStatusFileRes;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.EntityUserActionDAO;
import com.vedantu.user.pojos.UserInfo;

public class StatusFeedManager extends AbstractContentManager {

    public static StatusFeedManager INSTANCE             = new StatusFeedManager();

    public static final String      STATUS_FEED_TEMP_DIR = FileSystemConstants.TEMP_DIR
                                                                 + File.separator + "statusfeed";

    private StatusFeedManager() {

    }

    private final static ALogger LOGGER = Logger.of(StatusFeedManager.class);

    public String getStatusFeedURLFromUUID(String uuid) {

        String imageURL = ImageDisplayURLUtil.getStatusFeedTempImageURL(uuid
                + FileUtils.JPG_EXTENTION);

        return imageURL;
    }

    public AddStatusFeedRes addStatusFeed(AddStatusFeedReq request) throws VedantuException {

        Organization organization = OrganizationDAO.INSTANCE.getById(request.orgId,
                VedantuRecordState.ACTIVE);

        if (StringUtils.isNotEmpty(request.orgId) && organization == null) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }

        return addStatusFeed(request.userId, request.orgId, request.statusMessage, request.source,
                request.with);
    }

    public AddStatusFeedRes addStatusFeed(String userId, String orgId, String statusMessage,
            Source source, List<ShareWithEntity> with) throws VedantuException {

        if (StringUtils.isEmpty(statusMessage) && source == null) {

            LOGGER.debug(" No source no statusMessage provided " + source);

            throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
        }
        User u = UserDAO.INSTANCE.findUserById(userId);
        if (u == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
 

        LOGGER.debug(" Source provided " + source);

        if (source != null) {

            File fileToBeUploaded = null;

            String uuid = UUID.randomUUID().toString();
            if (source.linkType == LinkType.UPLOADED) {
                fileToBeUploaded = ImageDisplayURLUtil.getEmbededFileName(STATUS_FEED_TEMP_DIR,
                        source.url);
            } else if (source.linkType == LinkType.ADDED) {

                if (StringUtils.isNotEmpty(source.image)) {
                    String imageName = uuid + FileUtils.JPG_EXTENTION;
                    String imageTempOutputPath = getImageLocalTempPath(imageName);
                    LOGGER.info("downloading image to : " + imageTempOutputPath);
                    fileToBeUploaded = DownloadImageManager.downloadImage(source.image,
                            imageTempOutputPath, FileUtils.JPG_EXTENTION_WITHOUT_DOT);

                }
            }

            if (fileToBeUploaded == null && StringUtils.isNotEmpty(source.image)) {

                throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
            }
            if (fileToBeUploaded != null) {
                StatusFeedEntityFileStorage storage = new StatusFeedEntityFileStorage();
                StorageResult uploadResult = null;
                try {

                    Map<String, String> tags = new HashMap<String, String>();
                    tags.put(ConstantsGlobal.USER_ID, userId);
                    tags.put(ConstantsGlobal.ENTITY_TYPE, EntityType.STATUSFEED.name());
                    tags.put(ConstantsGlobal.ORG_ID, orgId);

                    uploadResult = storage.storeImage(uuid, fileToBeUploaded,
                            FileCategory.CONVERTED, ImageSize.ORIGINAL, tags);
                } catch (EntityFileStorageException e) {
                    LOGGER.error("Could not upload file to OS fileName:"
                            + fileToBeUploaded.getAbsolutePath());
                    throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
                }

                LOGGER.info("image " + fileToBeUploaded.getAbsolutePath()
                        + " copied to object store : " + uploadResult.isStored);

                if (uploadResult.isStored) {
                    LOGGER.info("adding image : " + uploadResult.fileId + " to statusFeed");
                    source.image = uploadResult.uuid;

                }
                fileToBeUploaded.delete();
            }

            source.caption = StringUtils.defaultIfEmpty(source.caption, StringUtils.EMPTY);
        }

        StatusFeed statusFeed = new StatusFeed(userId, statusMessage);

        statusFeed.sourceContent = source;
        if (StringUtils.isNotEmpty(orgId)) {
            statusFeed.contentSrc = new SrcEntity(EntityType.ORGANIZATION, orgId);
        }
        StatusFeedDAO.INSTANCE.save(statusFeed);

        EntityShare entityShared = EntityShareDAO.INSTANCE.addShare(orgId, userId, new SrcEntity(
                EntityType.STATUSFEED, statusFeed._getStringId()), with, null, ShareType.SHARE);
        statusFeed.shares = entityShared.with.size();
        ShareEntityDetails details = new ShareEntityDetails();
        details.entity = entityShared.entity;
        details.userId = entityShared.userId;
        details.with = new HashSet<ShareWithEntity>();
        for (SrcEntity sectionLevelEntity : entityShared.with) {
            details.with.add(new ShareWithEntity(sectionLevelEntity.type, sectionLevelEntity.id));
        }

        details.type = entityShared.type;
        details.content = entityShared.content;
        details.userAction = (entityShared.type == ShareType.ASKED ? UserActionType.ASKED
                : UserActionType.SHARED);

        generateEventAysc(userId, details, EventType.SHARE_ENTITY);

        UserSecuritySet useSecuritySet = new UserSecuritySet(userId, orgId);
        NewsFeedSecurityVaildator.set(useSecuritySet);

        //
        NewsFeedInfo statusFeedInNewsFeed = new NewsFeedInfo(details.toNewsActivity());

        statusFeedInNewsFeed.why = NotificationReason.ACTOR;
        statusFeedInNewsFeed.newsActivityId = StringUtils.EMPTY;
        statusFeedInNewsFeed.newsFeedId = StringUtils.EMPTY;
        statusFeedInNewsFeed.time = statusFeed.timeCreated;
        statusFeedInNewsFeed.eType = EventType.SHARE_ENTITY;

        List<NewsFeedInfo> statusFeedList = new ArrayList<NewsFeedInfo>();
        statusFeedList.add(statusFeedInNewsFeed);
        statusFeedList = NewsAggregatorHelper.populateDetails(statusFeedList, userId);
        AddStatusFeedRes response = new AddStatusFeedRes();
        response.list.add(statusFeedList.get(0));
        return response;
    }

    public UploadStatusFileRes uploadImageTest(UploadStatusFileReq request) throws VedantuException {

        EntityType entityType = request.type;
        if (entityType != EntityType.STATUSFEED) {
            LOGGER.error("not a valid Invalid StatusFeedId: ");
            throw new VedantuException(VedantuErrorCode.INVALID_ENTITY_TYPE);
        }
        if (!new ImageFilter().accept(new File(request.imageFileName))) {
            String errorMsg = "Invalid image File " + request.imageFileName;
            LOGGER.error(errorMsg);
            throw new VedantuException(VedantuErrorCode.UNSUPPORTED_FILE_TYPE, errorMsg);
        }
        LOGGER.info("Uploading image  : " + request.imageFile.getAbsolutePath()
                + " for statusFeed: ");

        String uuid = UUID.randomUUID().toString();
        String newFileName = uuid + FileUtils.JPG_EXTENTION;
        String tempFilePath = FileSystemFactory.INSTANCE.getTempFS().getFilePath(
                entityType.name().toLowerCase(), newFileName);
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            in = new FileInputStream(request.imageFile);
            File dstFile = new File(tempFilePath);
            out = new FileOutputStream(dstFile);
            IOUtils.copy(in, out);
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            LOGGER.error(e.getMessage(), e);

            throw new VedantuException(VedantuErrorCode.FILE_NOT_FOUND);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            Logger.error(e.getMessage(), e);

            throw new VedantuException(VedantuErrorCode.UPLOAD_ERROR);
        } finally {
            // IOUtils will take care of null check for the in/out stream;
            LOGGER.info("closing input stream");
            IOUtils.closeQuietly(in);
            LOGGER.info("closing output stream");
            IOUtils.closeQuietly(out);
            request.imageFile.delete();
        }

        UploadStatusFileRes response = new UploadStatusFileRes();
        response.uuid = uuid;
        response.url = ImageDisplayURLUtil.getStatusFeedTempImageURL(newFileName);

        return response;
    }

    private String getImageLocalTempPath(String fileName) {

        LocalFileSystemHandler tempLocHandler = (LocalFileSystemHandler) FileHandlerFactory
                .get(HandlerType.TEMP);
        return tempLocHandler.getDirectory() + LocalFileSystemHandler.PATH_SEPARATOR + fileName;

    }

    public GetStatusFeedRes getStatusFeed(GetStatusFeedReq request) throws VedantuException {

        GetStatusFeedRes response = new GetStatusFeedRes();
        UserSecuritySet securitySet = new UserSecuritySet(request.userId, request.orgId);

        SrcEntity statusFeedEntity = new SrcEntity(EntityType.STATUSFEED, request.feedId);
        if (securitySet.verifyShares(statusFeedEntity)) {
            StatusFeedInfo feedInfo = StatusFeedDAO.INSTANCE.getBasicInfo(request.feedId);
            feedInfo.voted = EntityUserActionDAO.INSTANCE.getEntityUpvote(request.userId,
                    feedInfo.id);
            if (StringUtils.isNotEmpty(request.orgId)) {

                UserInfo info = (UserInfo) getUserInfo(request.orgId, feedInfo.srcOwner.id);
                info.id = info instanceof OrgMemberBasicInfo ? ((OrgMemberBasicInfo) info).userId
                        : info.id;
                feedInfo.srcOwner = info;
            }
            if (feedInfo.sourceContent != null && feedInfo.sourceContent.linkInfo != null) {

                feedInfo.sourceContent.linkInfo.populate();
            }
            response.info = feedInfo;

        }
        if (response.info == null) {
            throw new VedantuException(VedantuErrorCode.ACTIVITY_NOT_AVAILABLE);

        }
        return response;

    }

    public DeleteStatusFeedRes delete(DeleteStatusFeedReq request) throws VedantuException {

        DeleteStatusFeedRes response = new DeleteStatusFeedRes();
        OrgMember deletionRequester = OrgMemberDAO.INSTANCE.getMemberByUserId(request.orgId,
                request.userId);
        String userId = StatusFeedDAO.INSTANCE.getOwnerId(request.id);
        if (deletionRequester == null) {
            throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND);
        }
        if (userId == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_STATUS_FEED);
        }
        boolean deletionAllowed = false;
        if (deletionRequester.profile == OrgMemberProfile.STUDENT
                && deletionRequester.userId.equals(userId)) {
            deletionAllowed |= true;
        }

        if (deletionRequester.profile == OrgMemberProfile.TEACHER
                || deletionRequester.profile == OrgMemberProfile.MANAGER) {
            deletionAllowed |= true;
        }

        if (!deletionAllowed) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED);
        }

        SrcEntity content = new SrcEntity(EntityType.STATUSFEED, request.id);
        response.deleted = delete(request.userId, EventType.UNKNOWN, content);

        return response;

    }
}
