import play.Application;

import com.vedantu.VedantuGlobalSettings;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.daos.OrgCenterDAO;
import com.vedantu.organization.daos.OrgProgramDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;

public class Global extends VedantuGlobalSettings {

    @Override
    public void onStart(Application app) {

        entityDAOFactoryRegister();

    }

    private void entityDAOFactoryRegister() {

        // DAO REGISTRATIONS

        EntityTypeDAOFactory.INSTANCE.register(EntityType.ORGANIZATION, OrganizationDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.PROGRAM, OrgProgramDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.CENTER, OrgCenterDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.SECTION, OrgSectionDAO.INSTANCE);
        EntityTypeDAOFactory.INSTANCE.register(EntityType.PLAN, LicensingPlanDAO.INSTANCE);
        // Non contents

    }

}
