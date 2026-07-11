package com.vedantu.commons.entity.factory;

import java.util.HashMap;
import java.util.Map;

import play.Logger;
import play.Logger.ALogger;

import com.vedantu.commons.entity.storage.AbstractEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSDocumentFileStorage;
import com.vedantu.commons.entity.storage.CMDSFileStorage;
import com.vedantu.commons.entity.storage.CMDSQuestionEntityFileStorage;
import com.vedantu.commons.entity.storage.CMDSVideoStorage;
import com.vedantu.commons.entity.storage.CommentEntityFileStorage;
import com.vedantu.commons.entity.storage.CompoundMediaStorage;
import com.vedantu.commons.entity.storage.DiscussionEntityFileStorage;
import com.vedantu.commons.entity.storage.DocumentEntityFileStorage;
import com.vedantu.commons.entity.storage.ExportRecordEntityFileStorage;
import com.vedantu.commons.entity.storage.FileStorage;
import com.vedantu.commons.entity.storage.MessageEntityFileStorage;
import com.vedantu.commons.entity.storage.OrganizationEntityFileStorage;
import com.vedantu.commons.entity.storage.QuestionEntityFileStorage;
import com.vedantu.commons.entity.storage.SolutionEntityFileStorage;
import com.vedantu.commons.entity.storage.StatusFeedEntityFileStorage;
import com.vedantu.commons.entity.storage.SubjectiveAnswerEntityFileStorage;
import com.vedantu.commons.entity.storage.UserProfilePicEntityFileStorage;
import com.vedantu.commons.entity.storage.VideoEntityFileStorage;
import com.vedantu.commons.enums.EntityType;

public class EntityStorageFactory {

    private static final ALogger                       LOGGER                   = Logger.of(EntityStorageFactory.class);
    public static EntityStorageFactory                 INSTANCE                 = new EntityStorageFactory();

    private Map<EntityType, AbstractEntityFileStorage> entityTypeStorageFactory = new HashMap<EntityType, AbstractEntityFileStorage>();

    private EntityStorageFactory() {

        entityTypeStorageFactory.put(EntityType.DOCUMENT, new DocumentEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.PAGE,
        // new PageEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.DIAGRAM,
        // new DiagramEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.QUESTION, new QuestionEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.PLAYLIST,
        // new PlaylistEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.USER, new UserProfilePicEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.SOLUTION, new SolutionEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.QRSOURCE,
        // new SourceThumbnailEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.ORGANIZATION, new OrganizationEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.VIDEO, new VideoEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.PACKAGE,
        // new PackageEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.STATUSFEED, new StatusFeedEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.MESSAGE, new MessageEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.DISCUSSION, new DiscussionEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.COMMENT, new CommentEntityFileStorage());

        entityTypeStorageFactory.put(EntityType.CMDSQUESTION, new CMDSQuestionEntityFileStorage());

        entityTypeStorageFactory.put(EntityType.CMDSVIDEO, new CMDSVideoStorage());
        entityTypeStorageFactory.put(EntityType.CMDSDOCUMENT, new CMDSDocumentFileStorage());
        entityTypeStorageFactory.put(EntityType.DOCUMENT, new DocumentEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.CMDSFILE, new CMDSFileStorage());
        entityTypeStorageFactory.put(EntityType.FILE, new FileStorage());
        entityTypeStorageFactory.put(EntityType.SUBJECTIVEANSWER, new SubjectiveAnswerEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.EXPORTRECORD, new ExportRecordEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.COMPOUNDMEDIA, new CompoundMediaStorage());

    }

    public AbstractEntityFileStorage get(EntityType entityType) {

        LOGGER.debug("Getting basic information : " + entityType);
        if (entityTypeStorageFactory.containsKey(entityType)) {
            LOGGER.debug("Found EntityStorage information : " + entityType);
            return entityTypeStorageFactory.get(entityType);
        }

        LOGGER.debug("No storage class  found information : " + entityType);
        return null;
    }

}