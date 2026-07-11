package com.vedantu.comm.event.details;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import com.vedantu.comm.enums.ShareType;
import com.vedantu.commons.ShareWithEntity;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.Scope;
import com.vedantu.commons.news.EntityNewsInfo;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.content.models.AbstractBoardEntityTagModel;
import com.vedantu.content.search.details.EntityDetails;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

public class ShareEntityDetails extends EntityDetails {

	public Set<ShareWithEntity>	with;
	public ShareType			type;
	public String				content;

	//

	@Override
	public void fromJSON(JSONObject json) {
		super.fromJSON(json);
		@SuppressWarnings("unchecked")
		List<ShareWithEntity> with = (List<ShareWithEntity>) JSONUtils
				.getJSONAwareCollection(ShareWithEntity.class, json,
						ConstantsGlobal.WITH);
		this.with = new HashSet<ShareWithEntity>(with);
		String type = JSONUtils.getString(json, ConstantsGlobal.TYPE);
		if (StringUtils.isNotEmpty(type)) {
			this.type = ShareType.valueOf(type);
		}
		content = JSONUtils.getString(json, ConstantsGlobal.CONTENT);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public NewsActivity toNewsActivity() {
		NewsActivity activity = new NewsActivity();
		activity.actor = new SrcEntity(EntityType.USER, userId);
		activity.src = new SrcEntity(__getSrcEntity().type, __getSrcEntity().id);
		VedantuBasicDAO<VedantuBaseMongoModel, ObjectId> basicDAO = EntityTypeDAOFactory.INSTANCE
				.get(__getSrcEntity().type);
		VedantuBaseMongoModel model = basicDAO.getById(__getSrcEntity().id);
		if (model != null) {
			if (model instanceof AbstractBoardEntityTagModel) {
				AbstractBoardEntityTagModel entityModel = (AbstractBoardEntityTagModel) model;
				activity.srcOwner = new SrcEntity(EntityType.USER,
						entityModel.userId);
			}
		}
		activity.sharedWith = new ArrayList(with);
		activity.comments = content;
		activity.involved = null;
		activity.eType = null;
		activity.info = new EntityNewsInfo();
		activity.info.actionType = this.userAction;
		activity.scope= Scope.ORG;
		return activity;
	}
}
