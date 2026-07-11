import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.events.apis.EventRegistrar;
import com.vedantu.content.daos.AssignmentDAO;
import com.vedantu.content.daos.CommentDAO;
import com.vedantu.content.daos.DiscussionDAO;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.daos.ModuleDAO;
import com.vedantu.content.daos.QuestionDAO;
import com.vedantu.content.daos.SolutionDAO;
import com.vedantu.content.daos.StatusFeedDAO;
import com.vedantu.content.daos.TestDAO;
import com.vedantu.content.daos.VideoDAO;

public class Global extends VedantuGlobalSettings {

    @Override
    public void beforeStart(Application app) {

        super.beforeStart(app);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DISCUSSION, DiscussionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.QUESTION, QuestionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.COMMENT, CommentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SOLUTION, SolutionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.TEST, TestDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.ASSIGNMENT, AssignmentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO, VideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, DocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.STATUSFEED, StatusFeedDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE, FileDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.MODULE, ModuleDAO.INSTANCE);

        EventRegistrar.INSTANCE.add(EventType.INDEX_DISCUSSION);
        EventRegistrar.INSTANCE.add(EventType.INDEX_DOCUMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_FILE);
        EventRegistrar.INSTANCE.add(EventType.INDEX_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.INDEX_QUESTION);
        EventRegistrar.INSTANCE.add(EventType.INDEX_TEST);
        EventRegistrar.INSTANCE.add(EventType.INDEX_ASSIGNMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CHALLENGE);
        EventRegistrar.INSTANCE.add(EventType.VOTE_ENTITY);
        EventRegistrar.INSTANCE.add(EventType.FOLLOW_ENTITY);
        EventRegistrar.INSTANCE.add(EventType.INDEX_MODULE);
        EventRegistrar.INSTANCE.add(EventType.SEND_EMAIL);
    }
}
