package com.lms.models;

import com.lms.common.utils.ImageDisplayURLUtil;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.content.IIndexable;
import com.lms.common.vedantu.entity.storage.FileCategory;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.Scope;
import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.SrcType;
import com.lms.interfaces.ICMDSModel;
import com.lms.pojos.requests.CMDSDocumentInfo;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.util.Map;

@Document(value = "cmdsdocuments")
public class CMDSDocument extends Documents implements IIndexable, ICMDSModel {

	@Indexed
	public String globalDocId;
	public boolean publishingInProgress;
	Map<String, String> presetMap;

	public CMDSDocument() {

		super();
		published = false;
		globalDocId = null;
		scope = Scope.ORG;
		converted = false;
		recordState = VedantuRecordState.TEMPORARY;
		contentType = EntityType.CMDSDOCUMENT;
	}

	@Override
	public String toString() {

		String currentObjectString = super.toString();

		StringBuilder builder = new StringBuilder();
		if (!StringUtils.isEmpty(currentObjectString)) {
			builder.append(currentObjectString);
		}

		builder.append(" globalDocId : ").append(globalDocId);
		return builder.toString();
	}

	@Override
	public ModelBasicInfo toBasicInfo() {

		String orgId = (contentSrc != null) ? contentSrc.id : "";
		CMDSDocumentInfo info = new CMDSDocumentInfo(_getStringId(), name, EntityType.CMDSDOCUMENT, orgId, timeCreated,
				lastUpdated, this.userId, 0, published, completed, converted, globalDocId, recordState, linkType,
				this.getExportableSize());

		info.thumbnail = ImageDisplayURLUtil.getEntityImageURL(EntityType.CMDSDOCUMENT, info.thumbnail);

		if (linkType == SrcType.LinkType.ADDED) {
			info.url = url;
		} else if (converted) {
			info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSDOCUMENT, uuid);
		} else {
			info.url = ImageDisplayURLUtil.getEntityVideoURL(EntityType.CMDSDOCUMENT, uuid, extension,
					FileCategory.ORIGINAL);
		}

		return info;
	}

	@Override
	public String getGlobalId() {

		return globalDocId;
	}

	@Override
	public long getExportableSize() {

		if (size != null) {
			return size.getThumbnail() + size.getConverted();
		}
		return 0;
	}

}
