import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.cmds.daos.CMDSDocumentDAO;
import com.vedantu.cmds.daos.CMDSFileDAO;
import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.daos.ExportRecordDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.daos.DocumentDAO;
import com.vedantu.content.daos.FileDAO;
import com.vedantu.content.daos.VideoDAO;


public class Global extends VedantuGlobalSettings {

    @Override
    public void onStart(Application app) {

        EntityTypeDAOFactory.INSTANCE.register(EntityType.FILE, FileDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.VIDEO, VideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, DocumentDAO.INSTANCE);
        
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSFILE, CMDSFileDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CMDSVIDEO, CMDSVideoDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.DOCUMENT, CMDSDocumentDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.EXPORTRECORD, ExportRecordDAO.INSTANCE);
    }
}
