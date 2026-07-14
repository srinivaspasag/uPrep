package com.vedantu.content.daos;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.SolutionType;
import com.vedantu.content.models.Solution;
import com.vedantu.content.pojos.Attachment;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.user.daos.AbstractUserActionDAO;

public class SolutionDAO extends AbstractUserActionDAO<Solution, ObjectId> {

    private static final ALogger    LOGGER   = Logger.of(SolutionDAO.class);

    public static final SolutionDAO INSTANCE = new SolutionDAO();

    private SolutionDAO() {

        super(Solution.class);
    }

    public Solution addSolution(String userId, String qid, String content, List<String> answers,
            Map<String, List<String>> gridAnswer, SolutionType type, List<Attachment> attachments) throws VedantuException {

        Solution sol = new Solution(qid, userId, content, answers, type, attachments);
        sol.gridAnswer = gridAnswer;
        LOGGER.debug("saving solution : " + sol);
        save(sol);
        return sol;
    }

    public VedantuDBResult<Solution> getSolutions(String qId, String userId, int start, int size) {

        return getSolutions(qId, userId, start, size, SortOrder.DESC);
    }

    public VedantuDBResult<Solution> getSolutions(String qId, String userId, int start, int size,
            SortOrder orderByTime) {

        return getSolutions(qId, userId, start, size, orderByTime, false);
    }

    public VedantuDBResult<Solution> getSolutions(String qId, String userId, int start, int size,
            SortOrder orderByTime, boolean verifiedOnly) {

        return getSolutions(Arrays.asList(new String[] { qId }), userId, start, size, orderByTime,
                verifiedOnly);
    }

    public VedantuDBResult<Solution> getSolutions(Collection<String> qIds, String userId,
            int start, int size, SortOrder orderByTime, boolean verifiedOnly) {

        if (CollectionUtils.isEmpty(qIds)) {
            return new VedantuDBResult<Solution>();
        }

        DBObject query = new BasicDBObject(ConstantsGlobal.QID, new BasicDBObject(
                MongoManager.IN_QUERY, qIds.toArray()));
        if (verifiedOnly) {
            query.put(ConstantsGlobal.VERIFIED, verifiedOnly);
        }

        DBObject order = new BasicDBObject(ConstantsGlobal.TIME_CREATED, orderByTime.getValue());

        VedantuDBResult<Solution> result = getInfos(query, null, start, size, order);
        return result;
    }

    @Override
    public List<SrcEntity> getChildren(String id) {

        // TODO Auto-generated method stub
        return null;
    }

    public long getSolutionsCount(String questionId){
        long count = getQuery().filter("qId", questionId).filter("verified",true).order("-timeCreated").countAll();
        return count;
    }

    public Solution getByQuestionId(String questionId) {
        Solution solution = getQuery().filter("qId", questionId).filter("verified",true).order("-timeCreated").get();
        if (solution == null) {
            LOGGER.error("Cannot find solution with the quesiton id :" + questionId);
        }
        return solution;
    }


}
