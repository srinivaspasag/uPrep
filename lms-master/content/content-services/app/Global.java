import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.commons.entity.factory.EntityTypeContentManagerFactory;
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
import com.vedantu.content.daos.challenges.ChallengeDAO;
import com.vedantu.content.managers.DocumentManager;
import com.vedantu.content.managers.FileManager;
import com.vedantu.content.managers.ModuleManager;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.managers.TestManager;
import com.vedantu.content.managers.VideoManager;

public class Global extends VedantuGlobalSettings {

    @Override
    public void beforeStart(Application app) {

        super.beforeStart(app);

        EntityTypeDAOFactory.INSTANCE.register(EntityType.DISCUSSION, DiscussionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.QUESTION, QuestionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SOLUTION, SolutionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.TEST, TestDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.ASSIGNMENT, AssignmentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.COMMENT, CommentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO, VideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CHALLENGE, ChallengeDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.STATUSFEED, StatusFeedDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, DocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE, FileDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.MODULE, ModuleDAO.INSTANCE);

        EventRegistrar.INSTANCE.add(EventType.ADD_SOLUTION);
        EventRegistrar.INSTANCE.add(EventType.END_CHALLENGE);
        EventRegistrar.INSTANCE.add(EventType.END_TEST);
        EventRegistrar.INSTANCE.add(EventType.INDEX_TEST);
        EventRegistrar.INSTANCE.add(EventType.INDEX_ASSIGNMENT);
        EventRegistrar.INSTANCE.add(EventType.REMOVE_NEWS);
        EventRegistrar.INSTANCE.add(EventType.INDEX_CHALLENGE);
        EventRegistrar.INSTANCE.add(EventType.INDEX_DISCUSSION);
        EventRegistrar.INSTANCE.add(EventType.INDEX_QUESTION);
        EventRegistrar.INSTANCE.add(EventType.INDEX_VIDEO);
        EventRegistrar.INSTANCE.add(EventType.INDEX_DOCUMENT);
        EventRegistrar.INSTANCE.add(EventType.INDEX_FILE);
        EventRegistrar.INSTANCE.add(EventType.INDEX_MODULE);
        // TODO add INDEX_FILES

        EventRegistrar.INSTANCE.add(EventType.ATTEMPT_ENTITY);
        EventRegistrar.INSTANCE.add(EventType.ADD_COMMENT);
        EventRegistrar.INSTANCE.add(EventType.PROCESS_DOUBTS);
        registerContentManagers();
    }

    private void registerContentManagers() {

        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.VIDEO, VideoManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.FILE, FileManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.DOCUMENT,
                DocumentManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.QUESTION,
                QuestionManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.TEST, TestManager.class);
        EntityTypeContentManagerFactory.INSTANCE.register(EntityType.MODULE, ModuleManager.class);

    }

}
