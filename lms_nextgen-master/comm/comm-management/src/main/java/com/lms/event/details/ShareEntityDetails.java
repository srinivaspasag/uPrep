package com.lms.event.details;

import com.lms.common.ShareWithEntity;
import com.lms.common.news.EntityNewsInfo;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.enums.ShareType;
import com.lms.pojos.search.details.EntityDetails;
import common.utils.JSONUtils;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShareEntityDetails extends EntityDetails {

	public Set<ShareWithEntity> with;
	public ShareType type;
	public String content;

	//

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		@SuppressWarnings("unchecked")
		List<ShareWithEntity> with = (List<ShareWithEntity>) JSONUtils.getJSONAwareCollection(ShareWithEntity.class,
				json, ConstantsGlobal.WITH);
		this.with = new HashSet<ShareWithEntity>(with);
		String type = JSONUtils.getString(json, ConstantsGlobal.TYPE);
		if (StringUtils.isEmpty(type)) {
			this.type = ShareType.valueOf(type);
		}
		content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	public NewsActivity toNewsActivity() {
		NewsActivity activity = new NewsActivity();
		activity.actor = new SrcEntity(EntityType.USER, userId);
		activity.src = new SrcEntity(__getSrcEntity().type, __getSrcEntity().id);
		/*VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
				.get(__getSrcEntity().type);
		VedantuBaseMongoModel model = basicDAO.getById(__getSrcEntity().id);
		if (model != null) {
			if (model instanceof AbstractBoardEntityTagModel) {
				AbstractBoardEntityTagModel entityModel = (AbstractBoardEntityTagModel) model;
				activity.srcOwner = new SrcEntity(EntityType.USER, entityModel.userId);
			}
		}*/
		activity.sharedWith = new ArrayList(with);
		activity.comments = content;
		activity.involved = null;
		activity.eType = null;
		activity.info = new EntityNewsInfo();
		activity.info.actionType = this.userAction;
		activity.scope = Scope.ORG;
		return activity;
	}
}
