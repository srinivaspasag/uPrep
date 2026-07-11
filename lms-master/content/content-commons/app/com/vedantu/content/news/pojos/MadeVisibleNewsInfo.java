package com.vedantu.content.news.pojos;

import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.pojos.SrcEntity;

public class MadeVisibleNewsInfo extends EntityNewsInfo {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SrcEntity          target;

    @Override
    public void populate(String userId, String orgId) {

        //
        //        @SuppressWarnings("unchecked")
        //        VedantuBasicDAO<? extends VedantuBaseMongoModel, ObjectId> dao = EntityTypeDAOFactory.INSTANCE
        //                .get(target.type);
        //        if (dao == null) {
        //            Logger.debug("NO DAO found for " + target.type);
        //            return;
        //        }
        //
        //        VedantuBaseMongoModel mongoModel = dao.getById(target.id);
        //        if (mongoModel == null) {
        //            Logger.debug("NO content found for " + target.type + " " + target.id);
        //            return;
        //        }
        //
        //        SrcEntity detailedTarget = EntityNewsDetailsFactory.INSTANCE.getInstance(target.type);
        //        if (detailedTarget == null) {
        //            Logger.debug("NO news details found for " + target.type + " " + target.id);
        //            return;
        //        }
        //
        //        IPopulator populator = EntityDetailsPopulatorFactory.INSTANCE.get(target.type);
        //
        //        if (populator == null) {
        //            Logger.debug("NO news populator found for " + target.type + " " + target.id);
        //            return;
        //        }
        //
        //        Map<String, IVedantuModel> modelDetailMap = new HashMap<String, IVedantuModel>();
        //        modelDetailMap.put(target.id, mongoModel);
        //        target = populator.populate(null, null, target, modelDetailMap);

    }
}
