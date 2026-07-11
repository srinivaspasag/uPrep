package com.lms.common.vedantu.entity.storage.fact;

import com.lms.common.vedantu.entity.storage.AbstractEntityFileStorage;
import com.lms.common.vedantu.entity.storage.DocumentEntityFileStorage;
import com.lms.common.vedantu.entity.storage.OrganizationEntityFileStorage;
import com.lms.common.vedantu.enums.EntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
public class EntityStorageFactory {

    private static final Logger logger = LoggerFactory.getLogger(EntityStorageFactory.class);
    public static EntityStorageFactory                 INSTANCE                 = new EntityStorageFactory();

    private static Map<EntityType, AbstractEntityFileStorage> entityTypeStorageFactory = new HashMap<EntityType, AbstractEntityFileStorage>();

    private EntityStorageFactory() {

      // entityTypeStorageFactory.put(EntityType.DOCUMENT, new DocumentEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.ORGANIZATION, new OrganizationEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.PAGE,
        // new PageEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.DIAGRAM,
        // new DiagramEntityFileStorage());
       /* entityTypeStorageFactory.put(EntityType.QUESTION, new QuestionEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.PLAYLIST,
        // new PlaylistEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.USER, new UserProfilePicEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.SOLUTION, new SolutionEntityFileStorage());
        // entityTypeStorageFactory.put(EntityType.QRSOURCE,
        // new SourceThumbnailEntityFileStorage());

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
        entityTypeStorageFactory.put(EntityType.EXPORTRECORD, new ExportRecordEntityFileStorage());
        entityTypeStorageFactory.put(EntityType.COMPOUNDMEDIA, new CompoundMediaStorage());*/

    }

    public  AbstractEntityFileStorage get(EntityType entityType) {

        logger.debug("Getting basic information : " + entityType);
        if (entityTypeStorageFactory.containsKey(entityType)) {
            logger.debug("Found EntityStorage information : " + entityType);
            return entityTypeStorageFactory.get(entityType);
        }

        logger.debug("No storage class  found information : " + entityType);
        return null;
    }
}
